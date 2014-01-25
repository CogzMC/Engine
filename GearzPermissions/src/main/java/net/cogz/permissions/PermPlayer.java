package net.cogz.permissions;

import com.mongodb.DB;
import com.mongodb.DBObject;
import lombok.Getter;
import net.tbnr.gearz.activerecord.BasicField;
import net.tbnr.gearz.activerecord.GModel;

import java.util.List;
import java.util.Map;

/**
 * Created by Jake on 1/24/14.
 */
public class PermPlayer extends GModel {
    @Getter @BasicField public String prefix;
    @Getter @BasicField public String suffix;
    @Getter @BasicField public String nameColor;
    @Getter @BasicField public String tabColor;
    @Getter @BasicField private String name;
    @Getter @BasicField private List<PermGroup> groups;
    @Getter @BasicField private Map<String, Boolean> permissions;

    public PermPlayer() {
        super();
    }

    public PermPlayer(DB database) {
        super(database);
    }

    public PermPlayer(DB database, DBObject dBobject) {
        super(database, dBobject);
    }

    public PermPlayer(DB database, String name) {
        this(database);
        this.name = name;
    }

    public void addPlayerToGroup(PermGroup group) {
        this.groups.add(group);
    }

    public void removePlayerFromGroup(PermGroup group) {
        this.groups.remove(group);
    }

    public void addPermission(String perm, boolean value) {
        this.permissions.put(perm, value);
    }

    public void removePermission(String perm) {
        this.permissions.remove(perm);
    }
}
