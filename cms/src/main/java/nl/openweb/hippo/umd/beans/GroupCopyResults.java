package nl.openweb.hippo.umd.beans;

import java.util.ArrayList;
import java.util.List;

public class GroupCopyResults {

    private List<String> copiedGroups = new ArrayList<String>();
    private List<String> alreadyMemberOf = new ArrayList<String>();

    public List<String> getCopiedGroups() {
        return copiedGroups;
    }

    public void setCopiedGroups(List<String> copiedGroups) {
        this.copiedGroups = copiedGroups;
    }

    public List<String> getAlreadyMemberOf() {
        return alreadyMemberOf;
    }

    public void setAlreadyMemberOf(List<String> alreadyMemberOf) {
        this.alreadyMemberOf = alreadyMemberOf;
    }

}
