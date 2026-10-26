package com.example.task_api.repository;

import jakarta.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import org.junit.jupiter.api.Test;//告诉junit，被标记的这个方法是一个测试方法，需要被测试框架自动执行

import com.example.task_api.model.TaskEvent;
import com.example.task_api.model.TaskEventType;
import com.example.task_api.model.TaskStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
@DataJpaTest //只加载JPA，Entity和Repository
@ActiveProfiles ("test")
class TaskEventRepositoryTest {

    @Autowired
    private TaskEventRepository taskEventRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldSaveAndFindEventsByTaskIdInOrder(){
        long taskId=100L;

        TaskEvent startedEvent = new TaskEvent(
            taskId,
            TaskStatus.TODO,
            TaskStatus.IN_PROGRESS,
            TaskEventType.STARTED
        );

        TaskEvent completedEvent = new TaskEvent(
            taskId,
            TaskStatus.IN_PROGRESS,
            TaskStatus.COMPLETED,
            TaskEventType.COMPLETED
        );

        TaskEvent savedStartedEvent =
            taskEventRepository.saveAndFlush(startedEvent);//flush不等于最终提交事务。@DataJpaTest通常在测试结束后回滚事务，不会留下测试数据影响mysql

        TaskEvent savedCompletedEvent =
            taskEventRepository.saveAndFlush(completedEvent);


        assertNotNull(savedStartedEvent.getId());
        assertNotNull(savedCompletedEvent.getId());//JPA 已经识别这个 Entity；INSERT 没有因为非空字段或映射错误而失败；数据库生成了 ID。

        entityManager.clear();

        List<TaskEvent> history =
            taskEventRepository.findByTaskIdOrderByIdAsc(taskId);

        assertEquals(2, history.size());//清除一级缓存后仍然查询到两条记录，证明事件可以被repository从数据库查回

        assertEquals(TaskEventType.STARTED, history.get(0).getEventType());

        assertEquals(TaskEventType.COMPLETED, history.get(1).getEventType());

        assertEquals(TaskStatus.TODO, history.get(0).getFromStatus());
        assertEquals(TaskStatus.COMPLETED, history.get(1).getToStatus());

    }


}
