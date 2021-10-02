package dev.kaua.squash.Data.Account;

public class DtoAccount implements Comparable<DtoAccount>{
    public static final String ACCOUNT_ID_CRY = "account_id_cry";
    public static final int ACCOUNT_ACTIVE = 2;
    public static final int BUSINESS_ACCOUNT = 1;
    public static final int ACCOUNT_RESTRICTION = 1;
    public static final int ACCOUNT_DISABLE = 0;
    public static final int ACCOUNT_IS_STAFF = 2;
    public static final int NORMAL_ACCOUNT = 0;
    public static final int VERIFY_ACCOUNT = 1;
    private long account_id, id_user, verify, status, active, ad_points;
    private boolean support_visit, story_tutorial;
    private String account_id_cry, id_user_cry, google_auth;
    private String name_user, username, email, phone_user, password, verify_id;
    private String banner_user, profile_image, bio_user, url_user, following, followers, born_date, joined_date;
    private String message, verification_level;
    private String login_method, token, login_info, trust, date_time_login, UID, content;
    private String id_following, imageURL, id, typingTo, last_seen, report_to, report_from, report_reason;
    private String account_id_following, status_chat, search, last_chat, chat_id, reason_warn, placed, adds, type_acc;

    public DtoAccount (){}

    public DtoAccount(String login_method, String password, String device_login,
                      String trust, String date_time_login, long status, String placed, String adds) {
        this.login_method = login_method;
        this.password = password;
        this.login_info = device_login;
        this.trust = trust;
        this.date_time_login = date_time_login;
        this.status = status;
        this.placed = placed;
        this.adds = adds;
    }

    public DtoAccount(String account_id_cry, String email, String id, String imageURL, String status_chat, String typingTo, String last_seen, String verification_level, long active) {
        this.account_id_cry = account_id_cry;
        this.email = email;
        this.id = id;
        this.imageURL = imageURL;
        this.status_chat = status_chat;
        this.typingTo = typingTo;
        this.last_seen = last_seen;
        this.verification_level = verification_level;
        this.active = active;
    }

    public boolean isStory_tutorial() {
        return story_tutorial;
    }

    public void setStory_tutorial(boolean story_tutorial) {
        this.story_tutorial = story_tutorial;
    }

    public String getGoogle_auth() {
        return google_auth;
    }

    public void setGoogle_auth(String google_auth) {
        this.google_auth = google_auth;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getType_acc() {
        return type_acc;
    }

    public void setType_acc(String type_acc) {
        this.type_acc = type_acc;
    }

    public boolean isSupport_visit() {
        return support_visit;
    }

    public void setSupport_visit(boolean support_visit) {
        this.support_visit = support_visit;
    }

    public long getAd_points() {
        return ad_points;
    }

    public void setAd_points(long ad_points) {
        this.ad_points = ad_points;
    }

    public String getAdds() {
        return adds;
    }

    public void setAdds(String adds) {
        this.adds = adds;
    }

    public String getReport_to() {
        return report_to;
    }

    public String getPlaced() {
        return placed;
    }

    public void setPlaced(String placed) {
        this.placed = placed;
    }

    public void setReport_to(String report_to) {
        this.report_to = report_to;
    }

    public String getReport_from() {
        return report_from;
    }

    public void setReport_from(String report_from) {
        this.report_from = report_from;
    }

    public String getReport_reason() {
        return report_reason;
    }

    public void setReport_reason(String report_reason) {
        this.report_reason = report_reason;
    }

    public String getReason_warn() {
        return reason_warn;
    }

    public void setReason_warn(String reason_warn) {
        this.reason_warn = reason_warn;
    }

    public long getActive() {
        return active;
    }

    public void setActive(long active) {
        this.active = active;
    }

    public String getChat_id() {
        return chat_id;
    }

    public void setChat_id(String chat_id) {
        this.chat_id = chat_id;
    }

    public String getLast_chat() {
        return last_chat;
    }

    public void setLast_chat(String last_chat) {
        this.last_chat = last_chat;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public String getLast_seen() {
        return last_seen;
    }

    public void setLast_seen(String last_seen) {
        this.last_seen = last_seen;
    }

    public String getTypingTo() {
        return typingTo;
    }

    public void setTypingTo(String typingTo) {
        this.typingTo = typingTo;
    }

    public String getStatus_chat() {
        return status_chat;
    }

    public void setStatus_chat(String status_chat) {
        this.status_chat = status_chat;
    }

    public static final String DEFAULT = "default";
    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAccount_id_following() {
        return account_id_following;
    }

    public void setAccount_id_following(String account_id_following) {
        this.account_id_following = account_id_following;
    }

    public String getId_following() {
        return id_following;
    }

    public void setId_following(String id_following) {
        this.id_following = id_following;
    }

    public String getVerification_level() {
        return verification_level;
    }

    public String getDate_time_login() {
        return date_time_login;
    }

    public String getUID() {
        return UID;
    }

    public void setUID(String UID) {
        this.UID = UID;
    }

    public void setDate_time_login(String date_time_login) {
        this.date_time_login = date_time_login;
    }

    public void setVerification_level(String verification_level) {
        this.verification_level = verification_level;
    }

    public String getLogin_info() {
        return login_info;
    }

    public void setLogin_info(String login_info) {
        this.login_info = login_info;
    }

    public String getTrust() {
        return trust;
    }

    public void setTrust(String trust) {
        this.trust = trust;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getLogin_method() {
        return login_method;
    }

    public void setLogin_method(String login_method) {
        this.login_method = login_method;
    }

    public String getAccount_id_cry() {
        return account_id_cry;
    }

    public void setAccount_id_cry(String account_id_cry) {
        this.account_id_cry = account_id_cry;
    }

    public String getId_user_cry() {
        return id_user_cry;
    }

    public void setId_user_cry(String id_user_cry) {
        this.id_user_cry = id_user_cry;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getAccount_id() {
        return account_id;
    }

    public void setAccount_id(long account_id) {
        this.account_id = account_id;
    }

    public long getId_user() {
        return id_user;
    }

    public void setId_user(long id_user) {
        this.id_user = id_user;
    }

    public long getVerify() {
        return verify;
    }

    public void setVerify(long verify) {
        this.verify = verify;
    }

    public long getStatus() {
        return status;
    }

    public void setStatus(long status) {
        this.status = status;
    }

    public String getName_user() {
        return name_user;
    }

    public void setName_user(String name_user) {
        this.name_user = name_user;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone_user() {
        return phone_user;
    }

    public void setPhone_user(String phone_user) {
        this.phone_user = phone_user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getVerify_id() {
        return verify_id;
    }

    public void setVerify_id(String verify_id) {
        this.verify_id = verify_id;
    }

    public String getBanner_user() {
        return banner_user;
    }

    public void setBanner_user(String banner_user) {
        this.banner_user = banner_user;
    }

    public String getProfile_image() {
        return profile_image;
    }

    public void setProfile_image(String profile_image) {
        this.profile_image = profile_image;
    }

    public String getBio_user() {
        return bio_user;
    }

    public void setBio_user(String bio_user) {
        this.bio_user = bio_user;
    }

    public String getUrl_user() {
        return url_user;
    }

    public void setUrl_user(String url_user) {
        this.url_user = url_user;
    }

    public String getFollowing() {
        return following;
    }

    public void setFollowing(String following) {
        this.following = following;
    }

    public String getFollowers() {
        return followers;
    }

    public void setFollowers(String followers) {
        this.followers = followers;
    }

    public String getBorn_date() {
        return born_date;
    }

    public void setBorn_date(String born_date) {
        this.born_date = born_date;
    }

    public String getJoined_date() {
        return joined_date;
    }

    public void setJoined_date(String joined_date) {
        this.joined_date = joined_date;
    }

    @Override
    public int compareTo(DtoAccount o) {
        return o.getStatus_chat().compareTo(o.getStatus_chat());
    }
}
