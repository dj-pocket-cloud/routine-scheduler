package program;

public class CurrentDaysTasks {

    private boolean completed;
    private String task;
    private Main.PRIORITY priority;
    private Task taskRef;


    private CurrentDaysTasks() {
        //private constructor
    }

    public CurrentDaysTasks(boolean completed, String task, Main.PRIORITY priority, Task taskRef) {
        this.completed = completed;
        this.task = task;
        this.priority = priority;
        this.taskRef = taskRef;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public Main.PRIORITY getPriority() {
        return priority;
    }

    public void setPriority(Main.PRIORITY priority) {
        this.priority = priority;
    }

    public Task getTaskRef() {
        return taskRef;
    }

    public void setTaskRef(Task taskRef) {
        this.taskRef = taskRef;
    }
}
