package com.ithinkrok.minigames.task;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by paul on 02/01/16.
 * <p>
 * Holds a list of game tasks. Allows all the tasks on the list to be cancelled at once. Good for holding tasks that
 * should not be run if a state changes in the future.
 */
public class TaskList {

    private List<GameTask> tasks = new ArrayList<>();

    public void addTask(GameTask task) {
        if (task.getTaskState() == GameTask.TaskState.CANCELLED || task.getTaskState() == GameTask.TaskState.FINISHED)
            return;

        tasks.add(task);
        task.addedToTaskList(this);
    }

    public void removeTask(GameTask task) {
        tasks.remove(task);

        if (task.getTaskState() == GameTask.TaskState.CANCELLED || task.getTaskState() == GameTask.TaskState.FINISHED)
            return;

        task.removedFromTaskList(this);
    }

    public void cancelAllTasks() {
        List<GameTask> tasksCopy = new ArrayList<>(tasks);

        tasksCopy.forEach(GameTask::cancel);
    }
}
