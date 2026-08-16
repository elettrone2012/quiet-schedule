# QuietSchedule — Functional Specification

> Current consolidated specification — updated through 16 August 2026.

---

## 1. Purpose

QuietSchedule is an open-source native Android application that automatically enables and disables **Do Not Disturb (DND)** according to configurable weekly profiles.

The application is intentionally focused on deciding **when** DND should be active and which Android DND Priority exceptions should be allowed while a profile is active.

QuietSchedule does not manage Wi-Fi, mobile data, airplane mode, location, generic automation, or unrelated system settings.

---

## 2. Platform and compatibility

- Native Android application.
- Kotlin.
- Jetpack Compose.
- Material 3.
- Minimum supported Android version: **Android 11 / API 30**.
- `minSdk = 30`.
- English and Italian localization.
- The app follows the Android system light/dark theme.
- The app follows the Android system 12-hour / 24-hour time preference.

Android 11 support should be retained while it does not create disproportionate implementation or maintenance complexity.

---

## 3. Profiles

A profile is the main configuration unit.

Each profile contains:

- unique identifier;
- name;
- enabled/disabled state;
- one or more weekly time ranges;
- DND Priority exception settings.

An arbitrary number of profiles may be stored.

Multiple profiles may be enabled simultaneously only when their schedules do not overlap.

A duplicated profile always starts disabled.

---

## 4. Weekly time ranges

Each profile contains one or more time ranges.

A time range contains:

- one or more selected days of the week;
- start time;
- end time.

At least one day must be selected.

Individual time ranges do not have their own enabled/disabled state. Enabling a profile enables all its configured ranges.

### 4.1 Same-day only

QuietSchedule does **not** support overnight ranges.

A range may end exactly at midnight. Internally, end-of-day is represented as `24:00`, while the UI follows the system time format and may display it as:

```text
00:00
```

in 24-hour mode, or:

```text
12:00 AM
```

in 12-hour mode.

Examples:

```text
08:00 -> 17:00   VALID
00:00 -> 08:00   VALID
22:00 -> 24:00   VALID
22:00 -> 07:00   INVALID
23:00 -> 22:00   INVALID
08:00 -> 08:00   INVALID
00:00 -> 24:00   INVALID
```

`00:00 -> 24:00` is intentionally rejected: full-day schedules are not supported.

If multiple days are selected, the same same-day interval is repeated independently on each selected day.

Example:

```text
Mon-Fri
09:00 -> 17:00
```

means Monday 09:00-17:00, Tuesday 09:00-17:00, and so on through Friday.

### 4.2 Internal time representation

Scheduling times are represented as integer minutes from the start of the day:

```text
00:00 = 0
08:00 = 480
22:00 = 1320
24:00 = 1440
```

Rules:

```text
startMinute: 0..1439
endMinute:   1..1440
endMinute > startMinute
```

`1440` is allowed only as the end of a range.

### 4.3 Interval semantics

Internally, intervals are half-open:

```text
[start, end)
```

The start instant is included; the end instant is excluded.

Therefore these ranges are adjacent and do not conflict:

```text
A: 06:00 -> 08:00
B: 08:00 -> 10:00
```

At a midnight boundary, these ranges are also adjacent rather than overlapping:

```text
Monday  22:00 -> 24:00
Tuesday 00:00 -> 08:00
```

When multiple scheduling events occur at the same instant, QuietSchedule calculates the final desired state before applying DND, avoiding unnecessary OFF/ON transitions.

---

## 5. Conflicts

Profiles may be saved even if their schedules would conflict with other profiles while disabled.

Conflicts are enforced when:

- enabling a profile;
- saving changes to an already enabled profile.

Two enabled profiles must never contain overlapping intervals on the same day.

When activation or saving is rejected because of a conflict, the UI identifies at least:

- conflicting profile;
- day;
- overlapping time interval.

Within one profile, overlapping configured ranges are also rejected when saving the profile.

Adjacent ranges are allowed.

---

## 6. Enabling a profile

When the user enables a profile:

1. verify DND policy access;
2. validate the profile configuration;
3. check conflicts against all other enabled profiles;
4. reject activation if invalid or conflicting;
5. persist the enabled state;
6. immediately evaluate whether the current local day/time is inside one of the profile ranges;
7. calculate the final global QuietSchedule DND state;
8. apply DND if required;
9. recalculate future scheduling events;
10. update the optional status notification.

If the profile is enabled while already inside one of its ranges, DND is applied immediately.

---

## 7. Disabling a profile

When a profile is disabled:

1. persist the disabled state;
2. calculate whether another enabled profile is currently active;
3. apply the final desired DND state;
4. recalculate future scheduling events;
5. update the optional status notification.

DND must not be blindly switched OFF if another enabled profile is currently active.

---

## 8. Manual DND changes

QuietSchedule does not continuously monitor DND and does not try to determine whether a DND change was manual or automatic.

At each QuietSchedule scheduling event, the application calculates and applies the desired state.

If the user changes DND manually between QuietSchedule events, QuietSchedule does not immediately override the user action.

---

## 9. DND mode and Priority exceptions

QuietSchedule uses Android **Priority** interruption mode when DND is active.

Each profile exposes the DND Priority options available in Android API 30.

All exception options default to **OFF** for a new profile.

### 9.1 Priority categories

Supported categories:

- alarms;
- reminders;
- events;
- media;
- system sounds;
- repeat callers;
- calls;
- messages;
- conversations.

### 9.2 Calls

When Calls is OFF, calls do not bypass DND because of the QuietSchedule policy.

When Calls is ON, the user chooses one sender scope:

- anyone;
- contacts;
- starred contacts.

### 9.3 Messages

When Messages is OFF, messages do not bypass DND because of the QuietSchedule policy.

When Messages is ON, the user chooses one sender scope:

- anyone;
- contacts;
- starred contacts.

### 9.4 Conversations

When Conversations is OFF, conversations do not bypass DND because of the QuietSchedule policy.

When Conversations is ON, the user chooses one conversation scope:

- anyone;
- important conversations.

### 9.5 Remaining categories

The following are independent boolean options, all default OFF:

- alarms;
- reminders;
- events;
- media;
- system sounds;
- repeat callers.

### 9.6 Suppressed visual effects

QuietSchedule exposes the non-deprecated specific visual-effect suppression options available by API 30:

- full-screen intents;
- notification lights;
- peek / heads-up presentation;
- status-bar appearance;
- notification badges;
- ambient-display appearance;
- notification-list appearance.

Each option is independent and defaults OFF.

The deprecated aggregate `SCREEN_ON` and `SCREEN_OFF` flags are not exposed.

QuietSchedule relies only on Android's standard DND/Notification Policy mechanisms. It does not maintain a contacts database and does not request contacts permission merely to configure Android sender scopes.

---

## 10. Multiple enabled profiles and DND policy

Enabled profiles cannot overlap in time.

Therefore, at any instant, at most one profile may be actively controlling DND.

This avoids having to merge DND exception policies from simultaneously active profiles.

Adjacent profiles are allowed. At a shared boundary, the policy of the profile beginning at that instant becomes effective without an unnecessary intermediate DND OFF state.

---

## 11. Device reboot

After device restart, QuietSchedule:

1. reloads stored configuration;
2. verifies relevant system permission state when execution resumes;
3. determines enabled profiles;
4. evaluates the current local day/time;
5. applies the currently required DND state and profile policy;
6. reconstructs future scheduling events;
7. updates the optional status notification.

---

## 12. Date, time zone and daylight-saving changes

QuietSchedule reacts to relevant system changes including:

- time-zone changes;
- manual date/time changes;
- daylight-saving transitions.

After such a change, schedules are recalculated from the current local date/time.

Weekly schedule definitions remain local wall-clock times.

---

## 13. Scheduling precision

QuietSchedule does **not** require Exact Alarms.

Scheduled activation/deactivation may therefore be delayed by Android because of mechanisms such as Doze, battery optimization, or power management.

The application must not claim exact-to-the-minute execution when the platform does not guarantee it.

Every relevant scheduling event recalculates the desired state from current persisted configuration and current local date/time rather than blindly applying stale event intent.

---

## 14. Permissions and privacy

Request only permissions/access strictly required for the implemented functionality.

Expected requirements include:

- DND / Notification Policy access;
- boot-related receiver support required to restore scheduling;
- notification permission on Android versions where required, if status notifications are enabled.

Do not request without a new explicit requirement:

- contacts;
- location;
- files/storage;
- Internet;
- Wi-Fi;
- Bluetooth;
- mobile data;
- calendar.

No:

- advertising;
- tracking;
- analytics;
- telemetry;
- proprietary cloud service.

---

## 15. DND permission revocation

If QuietSchedule detects that DND policy access is unavailable:

1. all profiles are persisted as disabled;
2. no profile may be enabled until permission is granted again;
3. continuous permission monitoring is not required;
4. detection when opening the app or at the next scheduled operation is sufficient;
5. future scheduling is cancelled/rebuilt as appropriate;
6. the UI informs the user.

Restoring permission does not restore previous enabled states automatically.

---

## 16. Persistence

QuietSchedule uses Android **DataStore** for local application configuration.

Persist at least:

- profiles;
- schedule ranges;
- enabled states;
- DND Priority exception settings;
- global application settings.

Persist domain configuration, not OS alarm identifiers as the source of truth.

OS scheduling must be reconstructible from persisted application state.

### 16.1 Schedule persistence format

Current persisted schedules use minute-based fields:

```text
startMinute
endMinute
```

Compatibility with previously persisted string-based `startTime` / `endTime` values is retained during migration.

Old valid data is converted to the minute-based domain model when read.

---

## 17. Backup and device migration

Maintain compatibility with standard Android application backup where practical.

After restored application data becomes available on another device:

1. validate restored data;
2. check system permissions again;
3. do not assume old scheduled OS events were transferred;
4. reconstruct scheduling from persisted profiles.

No proprietary backup service is used.

---

## 18. Main screen

The main screen displays:

- QuietSchedule application icon;
- application name;
- application version;
- profile list;
- enabled/disabled switch to the left of each profile;
- profile name;
- compact time-range summary;
- actions for new profile, settings and help.

Tapping a profile opens profile editing.

Example:

```text
[ON]  Work
      Mon-Fri 09:00 -> 17:00

[OFF] Evening
      Mon-Fri 18:00 -> 24:00
```

Displayed time follows the Android system 12/24-hour preference.

---

## 19. Profile editing

The profile editor contains at least:

- profile name;
- list of time ranges;
- add time range action;
- DND Priority exception settings;
- save action;
- duplicate action;
- delete action.

Saving an enabled profile requires conflict validation before commit.

A disabled profile may be saved even if it conflicts with another profile, but its own individual ranges must still be structurally valid and must not conflict with each other.

Unsaved changes are detected. Leaving the editor with pending changes requires explicit confirmation before discarding them.

---

## 20. Time range editing

The editor contains:

- day-of-week selection;
- start time;
- end time;
- save action;
- delete action.

Validation requirements:

- at least one day selected;
- `endMinute > startMinute`;
- no overnight interval;
- no zero-length interval;
- no full-day `00:00 -> 24:00` interval.

The native Material time picker follows the device's 12/24-hour setting.

In 12-hour mode:

```text
12:00 AM = midnight
12:00 PM = noon
```

When selected as the **end** time, midnight may represent end-of-day (`24:00` internally).

---

## 21. Profile deletion

Deletion requires explicit confirmation.

When deleting an enabled profile:

1. remove it;
2. recalculate the current desired DND state from remaining enabled profiles;
3. apply the resulting state/policy;
4. rebuild future scheduling;
5. update the optional status notification.

---

## 22. Profile duplication

Duplication copies:

- time ranges;
- all DND Priority exception settings;
- other profile-specific properties.

The duplicate always starts disabled.

A generated name such as `Work (copy)` may be used.

After duplication, open the new profile for editing.

---

## 23. Optional status notification

Global setting:

```text
Show status notification: ON/OFF
```

When active, the notification can show:

```text
QuietSchedule
Work - DND until 17:00
```

When enabled profiles exist but none is currently active:

```text
QuietSchedule
Next event: Work, Monday 09:00
```

When no profile is enabled:

```text
QuietSchedule inactive
```

Tapping the notification opens QuietSchedule.

No quick actions are included.

No permanent foreground service is introduced solely as an architectural convenience.

---

## 24. Global settings

Current global setting:

```text
Show status notification
```

Do not add configuration options without an explicit functional requirement.

---

## 25. Localization, time format and theme

Initial languages:

- English;
- Italian.

Displayed time format follows Android system preference:

- 24-hour mode, for example `22:00 -> 00:00`;
- 12-hour mode, for example `10:00 PM -> 12:00 AM`.

Theme follows Android system light/dark theme.

QuietSchedule does not duplicate these system preferences in its own settings.

---

## 26. Help and user guidance

The built-in guide documents at least:

- purpose of QuietSchedule;
- first-launch permissions;
- Android 11 / API 30 minimum compatibility;
- profiles;
- time ranges;
- midnight/end-of-day behavior;
- conflicts;
- DND Priority exceptions;
- calls/messages/conversations;
- third-party app limitations;
- notification visual effects;
- manual DND changes;
- reboot/time-zone/time-change behavior;
- scheduling precision;
- status notification;
- privacy;
- current product limitations;
- development assistance disclosure.

The guide explicitly explains that:

```text
10:00 PM -> 12:00 AM
```

is a valid same-day range ending at midnight, while:

```text
10:00 PM -> 8:00 AM
```

is an unsupported overnight range.

---

## 27. Distribution and product principles

Planned/current distribution model:

1. GitHub repository and GitHub Releases;
2. F-Droid later.

License:

```text
MIT
```

Design priorities:

1. simplicity;
2. deterministic behavior;
3. readable code;
4. minimal permissions;
5. no external service dependency;
6. transparency;
7. F-Droid compatibility;
8. no ads;
9. no tracking;
10. open-source implementation.

---

# Technical baseline

## Domain model

Conceptual model:

```text
Profile
├── id
├── name
├── enabled
├── dndPolicy
└── schedules[]
```

```text
Schedule
├── daysOfWeek
├── startMinute
└── endMinute
```

Hard Schedule invariants:

```text
daysOfWeek is not empty
startMinute in 0..1439
endMinute in 1..1440
endMinute > startMinute
not (startMinute == 0 and endMinute == 1440)
```

Do not add an `isOvernight` property.

Do not normalize or silently split an invalid overnight schedule.

---

## Scheduling

No Exact Alarm requirement.

Implementation must be resilient to delayed execution.

Recalculate after:

- boot;
- date/time change;
- time-zone change;
- relevant scheduling event;
- profile enable/disable/edit/delete.

An end boundary at `1440` is scheduled as `00:00` at the start of the following calendar day.

---

## Persistence

Use DataStore.

Persist domain configuration, not OS alarm identifiers.

Backward compatibility with previously persisted string time fields should be retained while migration is relevant.

---

## UI principles

Compose UI should operate on immutable UI/domain state where practical.

Validation errors must be explicit, especially:

- no day selected;
- `endMinute <= startMinute`;
- unsupported full-day range;
- conflict on enable/save of an enabled profile.

Do not silently reinterpret `22:00 -> 07:00` as an overnight interval.

---

# Minimum acceptance tests

At minimum verify:

1. accept `08:00 -> 17:00`;
2. accept `00:00 -> 08:00`;
3. accept `22:00 -> 24:00`;
4. reject `22:00 -> 07:00`;
5. reject `08:00 -> 08:00`;
6. reject `00:00 -> 24:00`;
7. reject a range with no selected day;
8. accept one range applied to multiple selected days;
9. start is inclusive;
10. end is exclusive;
11. adjacent ranges do not conflict;
12. conflicts between enabled profiles are rejected;
13. disabled profiles may be saved even if they conflict with enabled profiles;
14. internal conflicts inside one profile are rejected;
15. `22:00 -> 24:00` remains active at `23:59`;
16. its END event occurs at `00:00` on the following calendar day;
17. Monday `22:00 -> 24:00` and Tuesday `00:00 -> 08:00` are adjacent and do not conflict;
18. persisted `endMinute = 1440` round-trips correctly;
19. legacy persisted string times migrate correctly;
20. DND Priority API-30 categories map correctly;
21. missing DND policy access disables persisted profiles;
22. reboot reconstructs current state and future scheduling;
23. time-zone/manual-time/DST changes trigger recalculation;
24. status notification setting persists;
25. Android 11 is supported;
26. Android system 12/24-hour format is respected;
27. light/dark system theme is respected;
28. English localization works;
29. Italian localization works;
30. no contacts, location, storage, Internet, analytics or telemetry permissions/features are introduced.

---

# Non-goals

Do not add unless explicitly approved:

- overnight schedules;
- full-day schedules;
- generic automation actions;
- network/cloud synchronization;
- analytics/telemetry;
- custom contact management;
- calendar integration;
- location-based rules;
- exact alarms;
- foreground service solely as an architectural convenience.

---

# Product goal

QuietSchedule does one thing:

> Automatically enable and disable Android Do Not Disturb according to simple, predictable, same-day weekly profiles.

It is not a generic automation application.
