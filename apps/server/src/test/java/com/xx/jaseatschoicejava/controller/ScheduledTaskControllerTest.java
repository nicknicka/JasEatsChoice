package com.xx.jaseatschoicejava.controller;

import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.entity.ScheduledTask;
import com.xx.jaseatschoicejava.mapper.ScheduledTaskLogMapper;
import com.xx.jaseatschoicejava.mapper.ScheduledTaskMapper;
import com.xx.jaseatschoicejava.service.impl.DynamicScheduleServiceImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ScheduledTaskControllerTest {

    @Mock
    private ScheduledTaskMapper scheduledTaskMapper;

    @Mock
    private ScheduledTaskLogMapper scheduledTaskLogMapper;

    @Mock
    private TaskScheduler taskScheduler;

    @Mock
    private ApplicationContext applicationContext;

    @Test
    public void createThenExecute_shouldPersistAndInvokeTaskBean() {
        Map<String, ScheduledTask> storedTasks = new HashMap<>();
        SuccessfulTask taskBean = new SuccessfulTask();
        DynamicScheduleServiceImpl service = new DynamicScheduleServiceImpl(
                scheduledTaskMapper,
                scheduledTaskLogMapper,
                taskScheduler,
                applicationContext
        );
        ScheduledTaskController controller = new ScheduledTaskController();
        ReflectionTestUtils.setField(controller, "dynamicScheduleService", service);

        when(scheduledTaskMapper.selectById("api-task")).thenAnswer(invocation -> storedTasks.get("api-task"));
        when(scheduledTaskMapper.insert(any(ScheduledTask.class))).thenAnswer(invocation -> {
            ScheduledTask task = invocation.getArgument(0);
            storedTasks.put(task.getId(), task);
            return 1;
        });
        when(scheduledTaskMapper.updateById(any(ScheduledTask.class))).thenReturn(1);
        when(taskScheduler.schedule(any(Runnable.class), any(Instant.class))).thenReturn(taskFuture());
        when(applicationContext.getBean(SuccessfulTask.class)).thenReturn(taskBean);

        ScheduledTask task = new ScheduledTask();
        task.setId("api-task");
        task.setTaskName("接口创建任务");
        task.setTaskCode("API_TASK");
        task.setTaskType("ONCE");
        task.setTaskClassName(SuccessfulTask.class.getName());
        task.setTaskMethodName("execute");

        ResponseResult<?> createResponse = controller.createTask(task);

        assertTrue(createResponse.getSuccess());
        assertTrue(storedTasks.containsKey("api-task"));
        assertEquals("RUNNING", storedTasks.get("api-task").getStatus());

        ResponseResult<?> executeResponse = controller.executeNow("api-task");

        assertTrue(executeResponse.getSuccess());
        ArgumentCaptor<Runnable> scheduledTasks = ArgumentCaptor.forClass(Runnable.class);
        verify(taskScheduler, org.mockito.Mockito.times(2))
                .schedule(scheduledTasks.capture(), any(Instant.class));
        List<Runnable> invocations = scheduledTasks.getAllValues();
        invocations.get(1).run();

        assertTrue(taskBean.invoked);
        assertEquals("SUCCESS", storedTasks.get("api-task").getLastExecuteResult());
    }

    @Test
    public void createTask_whenInsertFails_shouldReturnFailure() {
        Map<String, ScheduledTask> storedTasks = new HashMap<>();
        DynamicScheduleServiceImpl service = new DynamicScheduleServiceImpl(
                scheduledTaskMapper,
                scheduledTaskLogMapper,
                taskScheduler,
                applicationContext
        );
        ScheduledTaskController controller = new ScheduledTaskController();
        ReflectionTestUtils.setField(controller, "dynamicScheduleService", service);

        when(scheduledTaskMapper.selectById("api-task")).thenAnswer(invocation -> storedTasks.get("api-task"));
        when(scheduledTaskMapper.insert(any(ScheduledTask.class))).thenReturn(0);

        ScheduledTask task = new ScheduledTask();
        task.setId("api-task");
        task.setTaskName("无法落库的任务");
        task.setTaskType("ONCE");

        ResponseResult<?> response = controller.createTask(task);

        assertFalse(response.getSuccess());
        assertEquals("任务创建失败", response.getMessage());
    }

    private ScheduledFuture taskFuture() {
        return mock(ScheduledFuture.class);
    }

    public static class SuccessfulTask {

        private boolean invoked;

        public void execute() {
            invoked = true;
        }
    }
}
