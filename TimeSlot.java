public class TimeSlot {
    private final int day, startMinute, endMinute;

    public TimeSlot(int day, int startMinute, int endMinute) {
        if (day < 1 || day > 7) {
            throw new IllegalArgumentException("Day must be 1 (Monday) to 7 (Sunday).");
        }
        if (startMinute < 0 || endMinute > 1440 || startMinute >= endMinute) {
            throw new IllegalArgumentException("Start must be before end, within the same day.");
        }
        this.day = day;
        this.startMinute = startMinute;
        this.endMinute = endMinute;
    }

    public int getDay() {
        return day;
    }

    public int getStartMinute() {
        return startMinute;
    }

    public int getEndMinute() {
        return endMinute;
    }

    public boolean overlaps(TimeSlot other) {
        return day == other.day && startMinute < other.endMinute
                && other.startMinute < endMinute;
    }

    public static int parseTime(String text) {
        if (text == null || !text.trim().matches("[0-9]{2}:[0-9]{2}")) {
            throw new IllegalArgumentException("Use 24-hour time in HH:mm format, such as 09:30.");
        }
        String[] parts = text.trim().split(":");
        int hour = Integer.parseInt(parts[0]);
        int minute = Integer.parseInt(parts[1]);
        if (hour > 24 || minute > 59 || (hour == 24 && minute != 0)) {
            throw new IllegalArgumentException("Time must be between 00:00 and 24:00.");
        }
        return hour * 60 + minute;
    }

    public static String formatTime(int minute) {
        return String.format("%02d:%02d", minute / 60, minute % 60);
    }

    @Override
    public String toString() {
        String[] days = { "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday" };
        return days[day - 1] + " " + formatTime(startMinute) + "-" + formatTime(endMinute);
    }
}
