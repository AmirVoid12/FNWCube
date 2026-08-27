package iran.flame.network.cube.managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import iran.flame.network.cube.CubeBankAccount;
import iran.flame.network.cube.managers.yml.CubeBankStorage;

public class CubeBankManager {
    private final CubeBankStorage storage = new CubeBankStorage();
    private final List<CubeBankAccount> accounts = new ArrayList<>();

    public void loadOrCreate(UUID owner) {
        YamlConfiguration config = this.storage.getAccountsConfigs().get(owner);
        CubeBankAccount account;

        if (config == null) {
            account = new CubeBankAccount(owner, new HashMap<>());
            account.save();
        } else {
            HashMap<String, Integer> cubeCounts = new HashMap<>();
            ConfigurationSection cubesSection = config.getConfigurationSection("cubes");
            if (cubesSection != null) {
                for (String key : cubesSection.getKeys(false)) {
                    Integer amount = cubesSection.getInt(key);
                    cubeCounts.put(key, amount);
                }
            }
            account = new CubeBankAccount(owner, cubeCounts);
        }

        this.accounts.add(account);
    }

    public void unRegister(CubeBankAccount account) {
        this.accounts.remove(account);
    }

    public CubeBankAccount getAccountByUuid(UUID owner) {
        for (CubeBankAccount account : this.accounts) {
            if (!account.getOwner().equals(owner)) continue;
            return account;
        }
        return null;
    }

    public CubeBankStorage getStorage() {
        return this.storage;
    }

    public List<CubeBankAccount> getAccounts() {
        return this.accounts;
    }
}