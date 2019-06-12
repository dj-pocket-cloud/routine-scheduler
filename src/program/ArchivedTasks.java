package program;

public class ArchivedTasks {
    private String task;
    private Task taskRef;

    private ArchivedTasks() {
        //private constructor
    }

    public ArchivedTasks(String task, Task taskRef) {
        this.task = task;
        this.taskRef = taskRef;
    }

    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public Task getTaskRef() {
        return taskRef;
    }

    public void setTaskRef(Task taskRef) {
        this.taskRef = taskRef;
    }
}
