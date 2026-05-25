package com.contest.common.constant;

public interface CommonConstants {

    String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    String DATE_PATTERN = "yyyy-MM-dd";

    int ROLE_STUDENT = 0;
    int ROLE_ADMIN = 1;

    int STATUS_NORMAL = 0;
    int STATUS_FROZEN = 1;

    int NOTIFY_OFF = 0;
    int NOTIFY_ON = 1;

    int CONTEST_DRAFT = 0;
    int CONTEST_OPEN = 1;
    int CONTEST_CLOSED = 2;

    int CONTEST_PERSONAL = 0;
    int CONTEST_TEAM = 1;
    int CONTEST_BOTH = 2;

    int TEAM_FORMING = 0;
    int TEAM_SUBMITTED = 1;
    int TEAM_APPROVED = 2;
    int TEAM_REJECTED = 3;
    int TEAM_DISSOLVED = 4;

    int MEMBER_NORMAL = 0;
    int MEMBER_LEADER = 1;

    int MEMBER_PENDING = 0;
    int MEMBER_APPROVED = 1;
    int MEMBER_REJECTED = 2;

    int REG_PENDING = 0;
    int REG_APPROVED = 1;
    int REG_REJECTED = 2;
    int REG_CANCELLED = 3;

    int REG_PERSONAL = 0;
    int REG_TEAM = 1;

    int NOTIFY_REVIEW_RESULT = 0;
    int NOTIFY_TEAM_APPLY = 1;
    int NOTIFY_TEAM_RESULT = 2;
    int NOTIFY_CONTEST_CHANGE = 3;
    int NOTIFY_SYSTEM = 4;

    int NOTIFY_UNREAD = 0;
    int NOTIFY_READ = 1;

    int CMS_BANNER = 0;
    int CMS_ANNOUNCEMENT = 1;

    int CMS_HIDDEN = 0;
    int CMS_VISIBLE = 1;
}
