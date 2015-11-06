package nl.openweb.hippo.umd.beans;

import java.util.Collections;
import java.util.List;

public class UsersBean {

    private final List<UserBean> source;
    private final List<UserBean> target;

    public UsersBean(List<UserBean> source, List<UserBean> target) {
        this.source = Collections.unmodifiableList(source);
        this.target = Collections.unmodifiableList(target);
    }

    public List<UserBean> getSource() {
        return source;
    }

    public List<UserBean> getTarget() {
        return target;
    }

}
