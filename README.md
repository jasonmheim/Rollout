# Rollout

This is a personal project I developed to learn more about Android, but also to address what
I believe are significant deficiencies in the official app provided by the NYC Bike Share program.
It is currently available through the Google Play Store but only to beta users who sign up at:

https://groups.google.com/forum/#!forum/rollout-beta-users

## Features

* **Still useful with poor/no network.** This application uses Sync Adapters to efficiently keep
  the list of stations and their capacities up to date. It tells you what time the data were last
  synchronized so that you know how recent the changes are. If you're tired of trying to use the
  official app in the elevator of your building, you'll find this quite useful.
* **Commuter focused.** When you are riding to home or to work, you probably already know where
  you are going, so navigation isn't particularly helpful. However, what you don't know while
  you are riding is whether the station closest to your destination is already full. This app
  lets you record your home and work destinations. Once you tell the app that you are heading to
  one of these places, it will track the capacity of the stations closest to your destination.
  If the closest station fills up, it will notify you with a recommendation for the next best
  available station.
* **Haptic feedback.** You can choose to enable vibration on the Settings page. With these signals
  enabled, unique vibration patterns notify you when capacities change and when stations fill up,
  indicating when you should seek the secondary/tertiary station choices based on your current
  position and current availability.
* **Wearable friendly.** If you have an Android Wear device, the notifications and haptic feedback
  are delivered to the wearable. What's more, you can tell the app when you are searching for a
  bike, riding, or idle from the wearable notification. This lets you handle your commute entirely
  from the wearable; you never have to take out your phone.
* **Don't get fooled by duds.** Tired of dashing to a station with one bike left only to find the
  "red dot" indicating that it's a dud? This app tells you how many duds are in each station to
  save you the headache.
* **Full and Empty thresholds.** Some stations frequently have "pinned" docks or bikes; the system
  believes they are available, but they are actually stuck in a bad state. Alternatively, you
  may normally commuteduring times when capacities are extremely volatile. If you encounter these
  situations frequently, you can adjust the meaning of "full" or "empty" on the settings page to
  choose your stations more conservatively.

## Usage

This application operates in one of five modes:

* **Silenced.** All notifications are disabled. Data and location synchronization is infrequent
  and consumes the least battery.
* **Idle.** Notifications are active, but at standard priority. Data and location synchronization
  is handled on a best effort basis every five to ten minutes. 
* **Searching for a bike.** Notifications are at max priority, but with no haptic feedback. Data
  and location synchronization are very frequent to help you locate a bike quickly.
* **Roaming on a bike.** This means you have no specific destination. Notifications are at max
  priority. Data and location synchronization are at maximum frequency to keep you up to date as
  to which stations nearest you have capacity.
* **Riding to home/work.** This means you are riding to a specific desitination. Notifications
  are at max priority, and optionally include haptic feedback. Data and location synchronaization
  are at maximum frequency to keep you up to date as to which station nearest your destination
  you should choose.
