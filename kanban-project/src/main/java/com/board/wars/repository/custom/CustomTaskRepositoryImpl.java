package com.board.wars.repository.custom;

import com.board.wars.domain.Project;
import com.board.wars.domain.Task;
import com.board.wars.repository.base.BaseMultiInterface;
import com.board.wars.repository.base.BaseSingleInterface;
import com.mongodb.BasicDBObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.springframework.data.mongodb.core.query.Criteria.where;

@Repository
public class CustomTaskRepositoryImpl implements CustomTaskRepository{
    private final ReactiveMongoTemplate template;

    public CustomTaskRepositoryImpl(ReactiveMongoTemplate template) {
        this.template = template;
    }

    @Override
    public Mono<BaseMultiInterface.BaseTask<Task>> insertNewTaskToProject(Long code, String columnName, Task task) {
        Query query = new Query(where("code").is(code).and("columns.name").is(columnName));
        Update update = new Update().addToSet("columns.$[columnIndex].tasks", task).filterArray(Criteria.where("columnIndex.name").is(columnName));
        return template.findAndModify(query, update, FindAndModifyOptions.options().returnNew(true), Project.class).map(project -> () -> getTasksFromProjectColumn(project, columnName));
    }

    //TODO task name must be unique
    @Override
    public Mono<BaseSingleInterface.BaseTask<Task>> updateProjectTask(Long code, String columnName, Task task) {
        Query updateQuery = new Query(where("code").is(code).and("columns.name").is(columnName).and("columns.tasks.name").is(task.getName()));
        Update update = new Update().set("columns.$[columnIndex].tasks.$", task).filterArray(Criteria.where("columnIndex.name").is(columnName));
        return template.updateMulti(updateQuery, update, Project.class)
                .filter(result -> result.getModifiedCount() > 0)
                .flatMap(result -> findByProjectColumnTaskName(code, columnName, task.getName(), Pageable.unpaged()));
    }

    @Override
    public Mono<BaseMultiInterface.BaseTask<Task>> deleteProjectTask(Long code, String columnName, String taskName) {
        Query query = new Query(Criteria.where("code").is(code).and("columns.name").is(columnName));
        Update update = new Update().pull("columns.$[columnIndex].tasks", new BasicDBObject("name", taskName))
                .filterArray(Criteria.where("columnIndex.name").is(columnName));
         return template.findAndModify(query, update, Project.class).map(project -> () -> getTasksFromProjectColumn(project, columnName));
    }

    @Override
    public Mono<BaseSingleInterface.BaseTask<Task>> findByProjectColumnTaskName(Long code, String name, String taskName, Pageable pageable) {
        Query query = new Query(Criteria.where("code").is(code).and("columns.name").is(name).and("columns.tasks.name").is(taskName)).with(pageable);
        //query.fields().include("columns.tasks.$");
        return template.findOne(query, Project.class)
                .map(project -> () -> getTasksFromProjectColumn(project, name)
                        .stream().filter(task -> task.getName().equals(taskName))
                        .findFirst().orElseThrow());
    }

    @Override
    public Mono<BaseMultiInterface.BaseTask<Task>> findAllTaskFromProject(Long code, String columnName, Pageable pageable){
        Query query = new Query(Criteria.where("code").is(code).and("columns.name").is(columnName)).with(pageable);
        query.fields().include("columns.$");
        return template.findOne(query, Project.class).map(project -> () -> getTasksFromProjectColumn(project, columnName));
    }

    @Override
    public Mono<BaseMultiInterface.BaseTask<Task>> updateProjectColumnTasks(Long code, String columnName, Task[] tasks) {
        Query updateQuery = new Query(where("code").is(code));
        Update update = new Update().set("columns.$[columnIndex].tasks", tasks).filterArray(Criteria.where("columnIndex.name").is(columnName));
        return template.updateMulti(updateQuery, update, Project.class)
                .filter(result -> result.getModifiedCount() > 0)
                .flatMap(result -> findAllTaskFromProject(code, columnName, Pageable.unpaged()));
    }

    private List<Task> getTasksFromProjectColumn(Project project, String columnName) {
        return project.getColumns().stream().filter(column -> column.getName().equals(columnName)).findFirst().orElseThrow().getTasks();
    }
}
