package pac.hy_da.ivokylinks;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class LinkManager {
    private final Map<String, UUID> pendingCodes = new HashMap<>();
    private final Random random = new Random();

    public String generateCode(UUID uuid) {
        String code = String.format("%06d", random.nextInt(1000000));
        pendingCodes.put(code, uuid);
        return code;
    }

    public UUID getPlayerByCode(String code) {
        return pendingCodes.get(code);
    }

    public void removeCode(String code) {
        pendingCodes.remove(code);
    }
}