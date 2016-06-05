package party.treesquaredcode.android.makerhawaii.api;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by rht on 6/4/16.
 */
public class MakerSpace {
    @Expose
    private String website;
    @Expose
    private Location location;
    @Expose
    @SerializedName("post_title")
    private String name;
    @Expose
    @SerializedName("howtovisit")
    private String howToVisit;
    @Expose
    @SerializedName("membership_info")
    private String membershipInfo;
    @Expose
    private String classes;


    public static class Location {
        @Expose
        private String address;
        @Expose
        private String lat;
        @Expose
        private String lng;

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getLat() {
            return lat;
        }

        public void setLat(String lat) {
            this.lat = lat;
        }

        public String getLng() {
            return lng;
        }

        public void setLng(String lng) {
            this.lng = lng;
        }
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHowToVisit() {
        return howToVisit;
    }

    public void setHowToVisit(String howToVisit) {
        this.howToVisit = howToVisit;
    }

    public String getMembershipInfo() {
        return membershipInfo;
    }

    public void setMembershipInfo(String membershipInfo) {
        this.membershipInfo = membershipInfo;
    }

    public String getClasses() {
        return classes;
    }

    public void setClasses(String classes) {
        this.classes = classes;
    }
}
