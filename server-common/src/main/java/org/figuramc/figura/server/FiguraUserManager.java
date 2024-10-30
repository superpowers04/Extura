package org.figuramc.figura.server;

import org.figuramc.figura.server.events.Events;
import org.figuramc.figura.server.events.users.LoadPlayerDataEvent;
import org.figuramc.figura.server.events.users.SavePlayerDataEvent;
import org.figuramc.figura.server.events.users.UserLoadingExceptionEvent;
import org.figuramc.figura.server.utils.Either;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public final class FiguraUserManager {
    private final FiguraServer parent;
    private final HashMap<UUID, FiguraUser> users = new HashMap<>();
    private final LinkedList<UUID> expectedUsers = new LinkedList<>();
    private int pingsTickCounter = 0;

    public FiguraUserManager(FiguraServer parent) {
        this.parent = parent;
    }

    public FiguraUser getUserOrNull(UUID playerUUID) {
        return users.get(playerUUID);
    }

    public void onUserJoin(UUID player) {

    }

    public FiguraUser getUser(UUID player) {
        return users.computeIfAbsent(player, (p) -> loadPlayerData(player));
    }

    private CompletableFuture<FiguraUser> wrapHandle(Either<FiguraUser, FutureHandle> handle) {
        if (handle.isA()) return CompletableFuture.completedFuture(handle.a());
        FutureHandle futureHandle = handle.b();
        CompletableFuture<FiguraUser> future = futureHandle.future;
        if (future.isDone()) {
            try {
                FiguraUser user = future.join();
                handle.setA(user);
                return CompletableFuture.completedFuture(user);
            }
            catch (Exception e) {
                Events.call(new UserLoadingExceptionEvent(futureHandle.user, e));
            }
        }
        return future;
    }

    public void setupOnlinePlayer(UUID uuid) {
        FiguraUser user = getUser(uuid);
        expectedUsers.remove(uuid); // This is called either way just to remove it in case if it was first time initialization
        user.setOnline();
        user.update();
    }


    private FiguraUser loadPlayerData(UUID player) {
        LoadPlayerDataEvent playerDataEvent = Events.call(new LoadPlayerDataEvent(player));
        if (playerDataEvent.returned()) return playerDataEvent.returnValue();
        Path dataFile = parent.getUserdataFile(player);
        return FiguraUser.load(player, dataFile);
    }

    public void forEachUser(Consumer<FiguraUser> func) {
        users.forEach((id, user) -> {
            if (user.online()) {
                func.accept(user);
            }
        });
    }

    public void onUserLeave(UUID player) {
        users.computeIfPresent(player, (uuid, pl) -> {
            if (!Events.call(new SavePlayerDataEvent(pl)).isCancelled())
                pl.save(parent.getUserdataFile(pl.uuid()));
            pl.setOffline();
            return pl;
        });
    }

    public void close() {
        for (var user: users.values()) {
            if (!Events.call(new SavePlayerDataEvent(user)).isCancelled())
                user.save(parent.getUserdataFile(user.uuid()));
        }
        users.clear();
    }

    public void expect(UUID user) {
        if (!expectedUsers.contains(user)) {
            expectedUsers.add(user);
        }
    }

    public boolean isExpected(UUID user) {
        return expectedUsers.contains(user);
    }

    public void tick() {
        if (pingsTickCounter == 20) {
            forEachUser(user -> user.pingCounter().reset());
            pingsTickCounter = 0;
        }
        pingsTickCounter++;
    }

    private record FutureHandle(UUID user, CompletableFuture<FiguraUser> future) {}
}
