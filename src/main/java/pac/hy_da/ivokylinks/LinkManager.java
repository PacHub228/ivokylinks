package pac.hy_da.ivokylinks;

import java.security.SecureRandom;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class LinkManager {
    private static final long CODE_TTL_MILLIS = 10 * 60 * 1000L;

    private static class PendingCode {
        final UUID uuid;
        final long createdAt = System.currentTimeMillis();

        PendingCode(UUID uuid) {
            this.uuid = uuid;
        }

        boolean isExpired() {
            return System.currentTimeMillis() - createdAt > CODE_TTL_MILLIS;
        }
    }

    private final Map<String, PendingCode> pendingCodes = new ConcurrentHashMap<>();
    private final Map<UUID, String> codesByPlayer = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();

    public synchronized String generateCode(UUID uuid) {
        purgeExpired();

        String previous = codesByPlayer.remove(uuid);
        if (previous != null) pendingCodes.remove(previous);

        String code;
        do {
            code = String.format("%06d", random.nextInt(1_000_000));
        } while (pendingCodes.containsKey(code));

        pendingCodes.put(code, new PendingCode(uuid));
        codesByPlayer.put(uuid, code);
        return code;
    }

    public synchronized UUID getPlayerByCode(String code) {
        PendingCode pending = pendingCodes.get(code);
        if (pending == null) return null;
        if (pending.isExpired()) {
            removeCode(code);
            return null;
        }
        return pending.uuid;
    }

    public synchronized void removeCode(String code) {
        PendingCode pending = pendingCodes.remove(code);
        if (pending != null) {
            codesByPlayer.remove(pending.uuid, code);
        }
    }

    private void purgeExpired() {
        Iterator<Map.Entry<String, PendingCode>> it = pendingCodes.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, PendingCode> entry = it.next();
            if (entry.getValue().isExpired()) {
                it.remove();
                codesByPlayer.remove(entry.getValue().uuid, entry.getKey());
            }
        }
    }
}
