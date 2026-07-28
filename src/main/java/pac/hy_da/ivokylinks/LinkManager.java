package pac.hy_da.ivokylinks;

import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class LinkManager {
    private static final long CODE_TTL_MILLIS = 10 * 60 * 1000L;

    private static class PendingCode {
        final UUID uuid;
        final long createdAt = System.currentTimeMillis();
        PendingCode(UUID uuid) { this.uuid = uuid; }
        boolean isExpired() { return System.currentTimeMillis() - createdAt > CODE_TTL_MILLIS; }
    }

    private final Map<String, PendingCode> pendingCodes = new ConcurrentHashMap<>();
    private final Map<UUID, String> codesByPlayer = new ConcurrentHashMap<>();
    private final Random random = new Random();

    public synchronized String generateCode(UUID uuid) {
        String previous = codesByPlayer.get(uuid);
        if (previous != null) pendingCodes.remove(previous);

        String code;
        do {
            code = String.format("%06d", random.nextInt(1000000));
        } while (pendingCodes.containsKey(code));

        pendingCodes.put(code, new PendingCode(uuid));
        codesByPlayer.put(uuid, code);
        return code;
    }

    public UUID getPlayerByCode(String code) {
        PendingCode pending = pendingCodes.get(code);
        if (pending == null) return null;
        if (pending.isExpired()) {
            removeCode(code);
            return null;
        }
        return pending.uuid;
    }

    public void removeCode(String code) {
        PendingCode pending = pendingCodes.remove(code);
        if (pending != null) codesByPlayer.remove(pending.uuid, code);
    }
}
