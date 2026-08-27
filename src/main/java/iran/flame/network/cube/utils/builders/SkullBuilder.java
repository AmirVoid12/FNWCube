package iran.flame.network.cube.utils.builders;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;
import iran.flame.network.cube.GenCubes;
import iran.flame.network.cube.enums.XMaterial;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Base64;
import java.util.UUID;
import java.util.logging.Level;

public class SkullBuilder extends ItemBuilder {
    public SkullBuilder() {
        super(XMaterial.PLAYER_HEAD.get());
        this.setDamage((short) 3);
    }

    @SuppressWarnings("deprecation")
    public SkullBuilder setOwner(String owner) {
        SkullMeta skullMeta = (SkullMeta) this.itemStack.getItemMeta();
        assert skullMeta != null;

        if (owner.length() <= 16) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(owner);
            skullMeta.setOwningPlayer(offlinePlayer);
            this.itemStack.setItemMeta(skullMeta);
            return this;
        }

        return this.setTexture(owner);
    }

    private SkullBuilder setTexture(String texture) {
        SkullMeta skullMeta = (SkullMeta) this.itemStack.getItemMeta();
        assert skullMeta != null;

        PlayerProfile profile = Bukkit.getServer().createPlayerProfile(UUID.randomUUID(), null);
        PlayerTextures textures = profile.getTextures();

        try {
            String skinUrl = resolveSkinUrl(texture);
            URL url = URI.create(skinUrl).toURL();
            textures.setSkin(url);
            profile.setTextures(textures);
            skullMeta.setOwnerProfile(profile);
        } catch (MalformedURLException | IllegalArgumentException e) {
            GenCubes.getInstance().getLogger().log(Level.SEVERE, "Failed to set skull texture, invalid URL", e);
        } catch (Exception e) {
            GenCubes.getInstance().getLogger().log(Level.SEVERE, "Failed to set skull texture", e);
        }

        this.itemStack.setItemMeta(skullMeta);
        return this;
    }

    private String resolveSkinUrl(String texture) {
        if (texture.length() <= 64) {
            return "https://textures.minecraft.net/texture/" + texture;
        }

        String decodedJson = new String(Base64.getDecoder().decode(texture));
        int start = decodedJson.indexOf("\"url\":\"");
        if (start == -1) {
            return texture;
        }
        start += 7;
        int end = decodedJson.indexOf('"', start);
        return decodedJson.substring(start, end);
    }
}