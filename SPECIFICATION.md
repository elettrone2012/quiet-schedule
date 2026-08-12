# QuietSchedule v0.1 — Functional Specification

## 1. App Purpose

QuietSchedule is an open-source Android app that automatically enables and disables **Do Not Disturb (DND)** according to configurable weekly profiles.

The goal is to keep the app simple, transparent, and focused on one task only: deciding **when** DND should be active.

QuietSchedule does not manage Wi-Fi, mobile data, airplane mode, location, generic automation, or other system settings.

---

## 2. Profile Concept

A **profile** is the main configuration unit available to the user.

Each profile contains:

- name;
- enabled/disabled state;
- one or more weekly time ranges;
- DND exception settings for Android Starred/Favorite contacts.

Example:

```text
Profile: Work
Enabled: yes

Schedules:
Mon-Fri 21:00 → 08:00

Starred:
Calls: no
Messages: no
```

An arbitrary number of profiles may be saved.

Multiple profiles may be enabled at the same time only if their schedules do not overlap.

---

## 3. Enabled Profiles and Conflicts

Profiles may be saved even if their schedules potentially conflict.

Conflicts are checked only when a profile is enabled or when an already enabled profile is modified.

Two enabled schedules must never overlap at the same time.

Valid example:

```text
Work
Mon-Fri 21:00 → 08:00
ENABLED

Vacation
Every day 02:00 → 08:00
DISABLED
```

`Vacation` may be saved, but it cannot be enabled while `Work` creates an overlapping interval.

When a conflict occurs, the app must indicate:

- the conflicting profile;
- the day;
- the overlapping time range.

---

## 4. Time Ranges

Each profile contains one or more **time ranges**.

Individual time ranges do not have their own ON/OFF state.

When a profile is enabled, all of its time ranges are considered active.

Each time range contains:

- one or more days of the week;
- start time;
- end time.

At least one day must be selected.

`Start time = end time` is not allowed.

---

## 5. Time Range Interpretation

If:

```text
endTime > startTime
```

the time range ends on the same day.

Example:

```text
08:00 → 17:00
```

If:

```text
endTime < startTime
```

the time range ends on the following day.

Example:

```text
22:00 → 07:00
```

means:

```text
Monday 22:00 → Tuesday 07:00
```

If multiple days are selected, the same time range is repeated for each selected day.

Example:

```text
Mon-Sun
22:00 → 07:00
```

means DND is active every night from 22:00 to 07:00.

A time range such as:

```text
Mon-Sun
23:00 → 22:00
```

produces 23 hours of DND per day, leaving only the 22:00–23:00 interval outside DND.

---

## 6. Time Interval Model

Internally, time ranges must be treated as half-open intervals:

```text
[start, end)
```

The start instant is included and the end instant is excluded.

Therefore:

```text
Profile A: 06:00 → 08:00
Profile B: 08:00 → 10:00
```

do not conflict.

When multiple events occur at the same instant, QuietSchedule must first calculate the final desired state and only then modify DND.

It must therefore avoid unnecessary sequences such as:

```text
DND OFF
DND ON
```

when one time range ends at 08:00 and another begins at 08:00.

---

## 7. Enabling a Profile

When the user enables a profile:

1. QuietSchedule checks for conflicts with other enabled profiles.
2. If a conflict exists, activation is rejected.
3. If there are no conflicts, the profile is enabled.
4. QuietSchedule immediately checks whether the current time falls inside one of its time ranges.
5. If so, DND is enabled immediately.

Example:

```text
Profile: Vacation
02:00 → 08:00

05:30 user enables profile
05:30 QuietSchedule → DND ON
```

---

## 8. Disabling a Profile

If a profile is disabled while one of its time ranges is currently active:

```text
QuietSchedule → DND OFF
```

immediately.

The future schedule is then recalculated.

---

## 9. Manual DND Changes

QuietSchedule does not try to distinguish between manual and automatic DND changes.

It does not continuously monitor DND and does not attempt to override user actions.

Its behavior is deterministic:

```text
START time range → DND ON
END time range   → DND OFF
```

If the user manually changes DND during a time range, QuietSchedule does not intervene until the next scheduled event.

Example:

```text
23:00 QuietSchedule → DND ON
01:00 user          → DND OFF
07:00 QuietSchedule → DND OFF
```

The 07:00 operation may be redundant, but it remains part of the scheduled behavior.

---

## 10. DND Exceptions Using Starred Contacts

QuietSchedule does not maintain its own contact list.

It does not modify the user's contacts.

It does not add or remove contacts from Android Favorites/Starred contacts.

Each profile allows the user to decide whether **Android Starred/Favorite contacts** may bypass DND for:

- calls;
- messages.

The two options are independent.

Possible combinations:

```text
Calls NO
Messages NO
```

No exceptions.

```text
Calls YES
Messages NO
```

Only calls from Starred contacts may bypass DND.

```text
Calls NO
Messages YES
```

Only messages from Starred contacts may bypass DND.

```text
Calls YES
Messages YES
```

Calls and messages from Starred contacts may bypass DND.

QuietSchedule relies exclusively on DND functionality provided by Android.

---

## 11. Device Reboot

After the device restarts, QuietSchedule must:

1. reload saved profiles;
2. determine which profiles are enabled;
3. recalculate the current scheduling state;
4. if the current time falls inside an active time range, immediately apply DND;
5. schedule the next events.

Example:

```text
Profile: Night
23:00 → 07:00

03:00 device restarts
03:02 QuietSchedule becomes available
03:02 DND ON
```

---

## 12. Time, Time Zone, and Daylight Saving Changes

QuietSchedule must react to:

- daylight saving time changes;
- time zone changes;
- manual date/time changes.

After any of these events, it must recalculate schedules using the current local time.

The behavior must be transparent to the user.

---

## 13. Event Timing Precision

QuietSchedule **does not use Exact Alarms**.

Android may therefore delay scheduled activation or deactivation by a few minutes, especially due to:

- Doze;
- battery optimization;
- device power management;
- Android internal scheduling.

This limitation must be clearly documented.

The goal is to avoid additional permissions and keep implementation simple.

---

## 14. Permissions

QuietSchedule must request only the permissions strictly required for its operation.

In particular:

- access to DND / Notification Policy control;
- permissions required to restore schedules after device boot;
- notification permission where required by the Android version.

The app must not request, unless explicitly justified by a future feature:

- contacts;
- location;
- files;
- Internet;
- Wi-Fi;
- Bluetooth;
- mobile data;
- calendar.

The app must not use:

- tracking;
- analytics;
- telemetry;
- proprietary cloud services.

---

## 15. DND Permission Revocation

If QuietSchedule detects that DND access is no longer available:

1. all profiles are permanently set to `OFF`;
2. no profile may be enabled until the permission is granted again;
3. continuous permission monitoring is not required;
4. detecting the missing permission when the app is opened or during the next scheduled event is sufficient.

When the app is opened, a clear message must be displayed:

```text
Do Not Disturb permission is not available.

All profiles have been disabled.
Grant the permission before enabling them again.
```

When the permission is restored, profiles remain OFF.

The user must enable them again manually.

---

## 16. Data Persistence

QuietSchedule uses **DataStore** to store locally:

- profiles;
- time ranges;
- ON/OFF state;
- Starred call settings;
- Starred message settings;
- global settings.

No QuietSchedule account is required.

No proprietary cloud service is used.

---

## 17. Backup and Device Migration

Configurations should be compatible with Android's standard backup system.

When restoring the app on a new device:

1. app data is restored when available through Android backup;
2. QuietSchedule checks system permissions again;
3. scheduled events are reconstructed from profile data;
4. alarms from the old device are not directly transferred.

---

## 18. Main Screen

The main screen displays the list of profiles.

Example:

```text
QuietSchedule

[x] Work
    Mon-Fri 21:00 → 08:00

[x] Weekend
    Sat-Sun 23:00 → 10:00

[ ] Vacation
    Every day 02:00 → 08:00

[ ] Business Trip
    Tue-Wed-Thu 22:00 → 07:00

[ + New profile ]
```

Each item displays:

- name;
- time range summary;
- ON/OFF switch.

Tapping a profile opens the profile edit screen.

---

## 19. Profile Editing

The profile edit screen contains at least:

```text
Profile name
[Work]

Time ranges
- Mon-Fri 21:00 → 08:00

[ + Add time range ]

Starred exceptions
[x] Calls
[x] Messages

[ Save ]
```

The profile name can be edited directly.

Tapping a time range opens the time range edit screen.

---

## 20. Time Range Editing

The screen contains:

```text
Days

[M] [T] [W] [T] [F] [S] [S]

Start time
21:00

End time
08:00

[ Save time range ]
```

The user must also be able to delete the time range.

Conflict detection does not prevent saving a disabled profile.

---

## 21. Editing an Enabled Profile

When an enabled profile is modified, QuietSchedule must check all conflicts before accepting the changes.

If the new configuration creates a conflict:

```text
Save rejected
```

until the conflict is resolved.

---

## 22. Profile Deletion

Every profile deletion requires explicit confirmation.

Example:

```text
Delete profile "Work"?

This operation cannot be undone.

[Cancel] [Delete]
```

If the deleted profile is currently controlling DND:

1. DND is disabled;
2. the profile is deleted;
3. scheduling is recalculated.

---

## 23. Profile Duplication

A profile may be duplicated.

The duplicate must contain:

- all time ranges;
- Starred call settings;
- Starred message settings;
- any other profile properties.

The duplicate always starts as:

```text
OFF
```

to prevent immediate conflicts.

The initial name may be:

```text
Work (copy)
```

After duplication, the new profile edit screen is opened automatically so the user may rename it.

---

## 24. Status Notification

QuietSchedule may display an optional persistent status notification.

Global setting:

```text
Show status notification
ON / OFF
```

If a time range is currently active:

```text
QuietSchedule
Work — DND until 08:00
```

If no time range is currently active but enabled profiles exist:

```text
QuietSchedule
Next event: Weekend, Saturday 23:00
```

If no profile is enabled:

```text
QuietSchedule inactive
```

Tapping the notification opens the app.

No quick actions are included in v0.1.

The notification must not require a permanent foreground service unless this becomes technically necessary.

---

## 25. Global Settings

Version 0.1 contains only one global setting:

```text
Show status notification
```

No unnecessary configuration options should be added.

---

## 26. Language, Time Format, and Theme

Initial languages:

- English;
- Italian.

The displayed time format must automatically follow the Android system preference:

```text
21:00 → 08:00
```

or:

```text
9:00 PM → 8:00 AM
```

The theme must automatically follow Android:

- light;
- dark.

QuietSchedule must not duplicate these system settings inside the app.

---

# Technical Requirements

## Platform

- Native Android
- Minimum Android version: **Android 11 / API 30**
- Kotlin
- Jetpack Compose

Android 11 support must be maintained only as long as it does not significantly increase implementation or maintenance complexity.

If a future feature would require substantial complexity to retain Android 11 compatibility, either the feature or the minimum Android version must be reconsidered.

---

## Architecture

The architecture should remain simple and readable.

Suggested structure:

```text
QuietSchedule

UI
├── Home
├── ProfileEdit
├── ScheduleEdit
└── Settings

Domain
├── Profile
├── Schedule
├── ConflictChecker
└── ScheduleCalculator

Data
└── DataStore

Android
├── DndController
├── ScheduleManager
├── Alarm/Event Receiver
├── BootReceiver
└── TimeChangeReceiver
```

Avoid architectural layers that provide no concrete value.

---

## Conceptual Data Model

```text
Profile
├── id
├── name
├── enabled
├── allowStarredCalls
├── allowStarredMessages
└── schedules[]
```

```text
Schedule
├── id
├── daysOfWeek
├── startTime
└── endTime
```

Final class names may be changed during implementation.

---

# Mandatory Minimum Tests

Time calculation logic must be testable independently from Android.

Unit tests must cover at least:

1. same-day time range:

```text
08:00 → 17:00
```

2. overnight time range:

```text
22:00 → 07:00
```

3. every-day schedule:

```text
23:00 → 22:00
```

4. adjacent time ranges:

```text
A ends at 08:00
B starts at 08:00
```

5. conflict between enabled profiles;

6. conflicting disabled profile that remains saveable;

7. enabling a profile while already inside one of its time ranges;

8. disabling a profile while inside one of its time ranges;

9. editing an enabled profile;

10. deleting an enabled profile;

11. duplicating a profile;

12. daylight saving time change;

13. time zone change;

14. reboot during an active time range;

15. reboot outside an active time range;

16. DND permission revoked;

17. Starred exceptions:
    - none;
    - calls only;
    - messages only;
    - calls + messages;

18. backup/restore;

19. status notification ON/OFF;

20. system 12/24-hour format;

21. light/dark theme;

22. English/Italian localization.

---

# Design Principles

QuietSchedule must prioritize:

1. simplicity;
2. predictable behavior;
3. readable code;
4. minimal permissions;
5. no dependency on external services;
6. transparency toward the user;
7. F-Droid compatibility;
8. no advertising;
9. no tracking;
10. fully open-source code.

Every new feature must be evaluated against these principles.

If a feature requires invasive workarounds, excessive permissions, or a significant increase in complexity, it must be excluded from v0.1.

---

# License and Distribution

Planned license:

**MIT License**

Public GitHub repository.

Planned distribution:

1. GitHub Releases;
2. F-Droid at a later stage.

---

# v0.1 Goal

QuietSchedule v0.1 must do one thing:

> **Automatically enable and disable Do Not Disturb according to simple and predictable weekly profiles.**

It must not become a generic Android automation platform.