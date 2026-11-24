# Now & Again
 
**A deadline that resets.**
 
Some things matter because you do them regularly: calling a friend, watering the plants, going for a run. Now & Again helps you keep up with them. For each task you choose the **longest break you're okay with**. Do it as often as you like, but not less often than that.
 
It's not a to-do list. It's a gentle reminder of the things you don't want to let slip.
 
<!-- Add screenshots here, for example:
<p align="center">
  <img src="docs/screenshot-home.png" width="260" alt="Home screen" />
  <img src="docs/screenshot-widget.png" width="260" alt="Widget" />
</p>
-->
 
## How it works
 
1. **Add a task** and choose how long it may go undone, for example "Call Sam, at least every 3 days".
2. **Watch the bar.** It shows how much of your time is used up, with one segment for each day.
3. **Tap the check** when you've done it. The timer starts over.
That's the whole idea. Doing a task early is always fine, and it simply restarts the clock from today.
 
### What the colors mean
 
| Bar | Meaning |
|---|---|
| Green and full | You did it today |
| Black or white | There is still time |
| Orange | Due today, your last day |
| Red | Overdue |
 
The text next to each bar always says the same thing in words, so you never need to rely on color alone.
 
### The rules in detail
 
- **The day you tap Done is a free day.** The bar starts the next morning. If you complete a 3-day task on Monday, it's due on Thursday and turns overdue on Friday.
- **A new task counts from today.** You haven't done it yet, so the first day is already day one.
- **Overdue only after the due day ends.** A task due Thursday is still fine until midnight.
- **Made a mistake?** Open the task and change its **last done** date. You can also change the name and the interval, and the due date follows.
- **Deleted or completed by accident?** Tap **Undo** in the message at the bottom of the screen.
## Features
 
- **Tasks with a maximum interval** from 1 to 365 days, with quick choices like daily, weekly and every 2 weeks.
- **A progress bar with one segment per day**, so you can see the length of an interval at a glance.
- **A daily reminder** at a time you choose (9:00 by default). It lists the tasks that are due today or overdue, and it stays quiet when there's nothing to do. Tapping it opens the app.
- **A home screen widget** that shows your tasks, scrolls when there are many, and lets you tap the check without opening the app.
- **Light, dark or system theme.** The design is black and white, and color only appears when something needs attention.
- **English and German**, with a language picker in the settings on Android 13 and newer.

## Privacy
 
Everything stays on your device.
 
- No account, no sign-up
- No ads, no analytics, no tracking
- The app doesn't ask for internet access
- Your tasks are stored in a local database on your phone.
- The only permission the app may ask for is **notifications** (Android 13 and newer), so it can send the daily reminder.
 
## Install
 
- **Google Play:** *coming soon*
- **APK**: [GitHub Releases](https://github.com/ghostbyte-dev/now-and-again/releases)

**Requirements:** Android 10 (API 29) or newer.
 
## Tips
 
- **Add the widget** from the hint below your tasks, or from the settings. Long-pressing your home screen and choosing Widgets works too.
- **Tap a task** to edit it, change when you last did it, or delete it.
- **Scroll to the bottom** of the list. There's a little surprise.
## Build it yourself
 
You need a recent version of [Android Studio](https://developer.android.com/studio).
 
```bash
git clone https://github.com/<your-name>/<your-repo>.git
```
 
1. Open the folder in Android Studio and let Gradle sync.
2. Pick an emulator or a connected phone.
3. Press **Run**.
### Built with
 
- [Kotlin](https://kotlinlang.org/) and [Jetpack Compose](https://developer.android.com/jetpack/compose) with Material 3
- [Room](https://developer.android.com/training/data-storage/room) for local storage
- [WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager) for reminders and widget refreshes
- [Jetpack Glance](https://developer.android.com/jetpack/compose/glance) for the widget
- [Navigation Compose](https://developer.android.com/jetpack/compose/navigation)
### Project structure
 
```
data/            Room database, repository, settings
domain/          Date logic and the text labels for tasks
notifications/   Daily reminder
widget/          Home screen widget and its refresh jobs
ui/              Screens and shared components (home, settings, theme)
```
 
## Contributing
 
Bug reports and ideas are welcome. Please open an issue and describe what you saw and what you expected. The app is intentionally small, and it should do one thing well, so features that don't fit that idea may not be accepted.
 
## License
 
Now & Again is free software, released under the [GNU General Public License v3.0](LICENSE). You can use, study, share and modify it, and if you distribute a modified version, it has to stay under the same license and its source has to be available.
 
---
 
## A Ghostbyte Production

![Ghostbyte banner](https://github.com/ghostbyte-dev/pixelix/blob/c42cbda6ac92b80e3d4d6e8f726ac211c1b02de6/assets/ghostbyte-banner.png)

Pixelix is developed by Ghostbyte, an independent software studio based in Austria.

We’re Daniel and Emanuel, and we build open-source software that we use ourselves.

If you want to reach out, you can find us here:

Website: [ghostbyte.dev](https://ghostbyte.dev)  
Email: [hey@ghostbyte.dev](mailto:hey@ghostbyte.dev)
 
