package bean;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * 用户类
 */
@Getter
@Setter
@Accessors(chain = true)
public class User {
    private String id;       // 用户 ID
    private String username; // 用户名

    // 保存一个用户加入的所有小组的名字，
    // 主要用于踢掉前一个登录账号时，重新绑定前一个账号加入的小组，避免丢失所加入的小组
    private Set<String> groups = new HashSet<>();

    public User() {}

    public User(String id, String username) {
        this.id = id;
        this.username = username;
    }

    /**
     * 加入小组
     *
     * @param groupName 小组名字
     */
    public void joinGroup(String groupName) {
        groups.add(groupName);
    }

    /**
     * 离开小组
     *
     * @param groupName 小组名字
     */
    public void leaveGroup(String groupName) {
        groups.remove(groupName);
    }

    /**
     * 用户 ID 作为用户唯一性判断条件
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
