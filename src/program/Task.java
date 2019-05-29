package program;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Task {

    //TODO: Date variables might have to be changed depending on how the rest of the code is implemented
    //TODO: use csv files for saving objects in this class

    private String name;
    private String description;
    private Main.PRIORITY priority;
    private List<String> dates = new ArrayList<String>(); //string format: Date|boolean (boolean for completed status)
    private boolean archived;

    private Task() {
        //locked default constructor
    }

    public Task(String name, String description, Main.PRIORITY priority, List<String> dates, boolean archived) {
        this.name = name;
        this.description = description;
        this.priority = priority;
        this.dates.addAll(dates);
        this.archived = archived;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Main.PRIORITY getPriority() {
        return priority;
    }

    public void setPriority(Main.PRIORITY priority) {
        this.priority = priority;
    }

    public List<String> getDates() {
        return dates;
    }

    public boolean dateExists(LocalDate date) {
        String dateStr = date+"";
        for (int i = 0; i < dates.size(); i++) {
            if (dates.get(i).contains(dateStr)) {
                return true;
            }
        }
        return false;
    }

    public void addDate(LocalDate date) {
        dates.add(date+"|false"); //assume not completed
    }

    public void removeDate(LocalDate date) {
        //remove date regardless of completed status
        dates.remove(date+"|false"); dates.remove(date+"|true");
    }

    public Boolean getCompleted(LocalDate date) {
        String dateStr = date+"";
        for (int i = 0; i < dates.size(); i++) {
            if (dates.get(i).contains(dateStr) && dates.get(i).contains("true")) {
                return true;
            }
        }
        return false;
    }

    public void setCompleted(LocalDate date, boolean completed) {
        //remove opposite date entry then put the updated one in
        dates.remove(date+"|"+!completed);
        dates.add(date+"|"+completed);
    }

    public boolean getArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }

    @Override
    public String toString() {
        String dateString = dates.toString().replace("[", "").replace("]", "");
        return name+'¬'+description+'¬'+priority+'¬'+dateString+'¬'+archived;
    }
}
