package com.xx.jaseatschoicejava.service.impl;

import com.xx.jaseatschoicejava.entity.ScheduledTask;
import com.xx.jaseatschoicejava.entity.ScheduledTaskLog;
import com.xx.jaseatschoicejava.mapper.ScheduledTaskLogMapper;
import com.xx.jaseatschoicejava.mapper.ScheduledTaskMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.TaskScheduler;

import java.time.Instant;
import java.util.concurrent.ScheduledFuture;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DynamicScheduleServiceImplTest {

    private static final String TASK_ID = "task-1";

    @Mock
    private ScheduledTaskMapper scheduledTaskMapper;

    @Mock
    private ScheduledTaskLogMapper scheduledTaskLogMapper;

    @Mock
    private TaskScheduler taskScheduler;

    @Mock
    private ApplicationContext applicationContext;

    @InjectMocks
    private DynamicScheduleServiceImpl service;

    @Test
    public void executeNow_whenTaskSucceeds_shouldInvokeRealBeanAndRecordSuccess() {
        SuccessfulTask taskBean = new SuccessfulTask();
        ScheduledTask task = task(SuccessfulTask.class.getName(), "execute");
        task.setLastErrorMessage("旧错误");
        when(scheduledTaskMapper.selectById(TASK_ID)).thenReturn(task);
        when(scheduledTaskMapper.updateById(task)).thenReturn(1);
        when(applicationContext.getBean(SuccessfulTask.class)).thenReturn(taskBean);

        assertTrue(service.executeNow(TASK_ID));
        captureScheduledTask().run();

        assertTrue(taskBean.invoked);
        assertEquals("SUCCESS", task.getLastExecuteResult());
        assertNull(task.getLastErrorMessage());
        assertEquals(1, task.getExecuteCount().intValue());
        assertNotNull(task.getLastExecuteTime());

        ScheduledTaskLog taskLog = captureTaskLog();
        assertEquals("SUCCESS", taskLog.getExecuteStatus());
        assertEquals("任务执行成功", taskLog.getResultMessage());
    }

    @Test
    public void executeNow_whenTaskThrows_shouldRecordFailedAndError() {
        FailingTask taskBean = new FailingTask();
        ScheduledTask task = task(FailingTask.class.getName(), "execute");
        when(scheduledTaskMapper.selectById(TASK_ID)).thenReturn(task);
        when(scheduledTaskMapper.updateById(task)).thenReturn(1);
        when(applicationContext.getBean(FailingTask.class)).thenReturn(taskBean);

        assertTrue(service.executeNow(TASK_ID));
        captureScheduledTask().run();

        assertEquals("FAILED", task.getLastExecuteResult());
        assertEquals("业务执行失败", task.getLastErrorMessage());
        assertEquals(1, task.getFailCount().intValue());

        ScheduledTaskLog taskLog = captureTaskLog();
        assertEquals("FAILED", taskLog.getExecuteStatus());
        assertEquals("业务执行失败", taskLog.getErrorMessage());
        assertTrue(taskLog.getExceptionStack().contains("业务执行失败"));
    }

    @Test
    public void addAndStartTask_whenTaskDoesNotExist_shouldInsert() {
        ScheduledTask task = task(SuccessfulTask.class.getName(), "execute");
        task.setTaskType("ONCE");
        when(scheduledTaskMapper.selectById(TASK_ID)).thenReturn(null);
        when(scheduledTaskMapper.insert(task)).thenReturn(1);
        when(taskScheduler.schedule(any(Runnable.class), any(Instant.class)))
                .thenReturn(taskFuture());

        assertTrue(service.addAndStartTask(task));

        assertEquals("RUNNING", task.getStatus());
        verify(scheduledTaskMapper).insert(task);
        verify(scheduledTaskMapper, never()).updateById(task);
    }

    @Test
    public void addAndStartTask_whenTaskExists_shouldFailInsteadOfUpdate() {
        ScheduledTask task = task(SuccessfulTask.class.getName(), "execute");
        task.setTaskType("ONCE");
        when(scheduledTaskMapper.selectById(TASK_ID)).thenReturn(task);

        assertEquals(false, service.addAndStartTask(task));

        verify(scheduledTaskMapper, never()).insert(task);
        verify(scheduledTaskMapper, never()).updateById(task);
        verify(taskScheduler, never()).schedule(any(Runnable.class), any(Instant.class));
    }

    @Test
    public void addAndStartTask_whenInsertAffectsNoRows_shouldFail() {
        ScheduledTask task = task(SuccessfulTask.class.getName(), "execute");
        task.setTaskType("ONCE");
        when(scheduledTaskMapper.selectById(TASK_ID)).thenReturn(null);
        when(scheduledTaskMapper.insert(task)).thenReturn(0);

        assertEquals(false, service.addAndStartTask(task));

        verify(taskScheduler, never()).schedule(any(Runnable.class), any(Instant.class));
    }

    @Test
    public void updateAndRestartTask_whenTaskExists_shouldUpdateInsteadOfInsert() {
        ScheduledTask task = task(SuccessfulTask.class.getName(), "execute");
        task.setTaskType("ONCE");
        when(scheduledTaskMapper.selectById(TASK_ID)).thenReturn(task);
        when(scheduledTaskMapper.updateById(task)).thenReturn(1);
        when(taskScheduler.schedule(any(Runnable.class), any(Instant.class)))
                .thenReturn(taskFuture());

        assertTrue(service.updateAndRestartTask(task));

        verify(scheduledTaskMapper).updateById(task);
        verify(scheduledTaskMapper, never()).insert(task);
    }

    @Test
    public void updateAndRestartTask_whenUpdateAffectsNoRows_shouldFail() {
        ScheduledTask task = task(SuccessfulTask.class.getName(), "execute");
        task.setTaskType("ONCE");
        when(scheduledTaskMapper.selectById(TASK_ID)).thenReturn(task);
        when(scheduledTaskMapper.updateById(task)).thenReturn(0);

        assertEquals(false, service.updateAndRestartTask(task));

        verify(taskScheduler, never()).schedule(any(Runnable.class), any(Instant.class));
    }

    private ScheduledTask task(String className, String methodName) {
        ScheduledTask task = new ScheduledTask();
        task.setId(TASK_ID);
        task.setTaskName("测试任务");
        task.setTaskCode("TEST_TASK");
        task.setTaskClassName(className);
        task.setTaskMethodName(methodName);
        task.setExecuteCount(0);
        task.setFailCount(0);
        task.setLogExecution(true);
        return task;
    }

    private Runnable captureScheduledTask() {
        ArgumentCaptor<Runnable> captor = ArgumentCaptor.forClass(Runnable.class);
        verify(taskScheduler).schedule(captor.capture(), any(Instant.class));
        return captor.getValue();
    }

    private ScheduledTaskLog captureTaskLog() {
        ArgumentCaptor<ScheduledTaskLog> captor = ArgumentCaptor.forClass(ScheduledTaskLog.class);
        verify(scheduledTaskLogMapper).insert(captor.capture());
        return captor.getValue();
    }

    private ScheduledFuture taskFuture() {
        return org.mockito.Mockito.mock(ScheduledFuture.class);
    }

    public static class SuccessfulTask {

        private boolean invoked;

        public void execute() {
            invoked = true;
        }
    }

    public static class FailingTask {

        public void execute() {
            throw new IllegalStateException("业务执行失败");
        }
    }
}
