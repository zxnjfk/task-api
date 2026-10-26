package com.example.task_api.scheduler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import com.example.task_api.model.AsyncJobStatus;
import com.example.task_api.repository.AsyncJobRepository;
import com.example.task_api.worker.AsyncJobWorker;

@ExtendWith(MockitoExtension.class)
class AsyncJobDispatcherTest {

    @Mock
    private AsyncJobRepository asyncJobRepository;

    @Mock
    private AsyncJobWorker asyncJobWorker;

    @InjectMocks
    private AsyncJobDispatcher asyncJobDispatcher;

    @Test
    void shouldContinueProcessingAfterOneJobFails() {
        when(asyncJobRepository.findIdsByStatus(
                eq(AsyncJobStatus.PENDING),
                any(Pageable.class)
        )).thenReturn(List.of(10L, 20L));

        doThrow(new IllegalStateException("Job 10 failed"))
                .when(asyncJobWorker)
                .process(10L);

        asyncJobDispatcher.dispatchPendingJobs();

        ArgumentCaptor<Pageable> pageableCaptor =
                ArgumentCaptor.forClass(Pageable.class);

        verify(asyncJobRepository).findIdsByStatus(
                eq(AsyncJobStatus.PENDING),
                pageableCaptor.capture()
        );

        Pageable pageable = pageableCaptor.getValue();

        assertEquals(0, pageable.getPageNumber());
        assertEquals(20, pageable.getPageSize());

        InOrder processingOrder = inOrder(asyncJobWorker);

        processingOrder.verify(asyncJobWorker).process(10L);
        processingOrder.verify(asyncJobWorker).process(20L);
    }
}
