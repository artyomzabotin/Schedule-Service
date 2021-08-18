package epam.az.scheduler.controller;

import epam.az.scheduler.service.ScheduleService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ScheduleControllerTest {

    @Mock
    private ScheduleService scheduleService;

    @Spy
    @InjectMocks
    private ScheduleController scheduleController;

    @Test
    void getAllSchedules_schedulesExist_callIsProxiedToScheduleService() {

        // WHEN
        scheduleController.getAllSchedules();

        // THEN
        Mockito.verify(scheduleService).getAll();
    }

    @Test
    void getById_idIsProvided_callIsProxiedToScheduleService() {

        // GIVEN
        Long scheduleId = 1L;

        // WHEN
        scheduleController.getById(scheduleId);

        // THEN
        Mockito.verify(scheduleService).getById(scheduleId);
    }

    @Test
    void getActiveWorkers_anyState_callIsProxiedToScheduleService() {

        // WHEN
        scheduleController.getActiveWorkers();

        // THEN
        Mockito.verify(scheduleService).callForActiveWorkers();
    }

    @Test
    void getFullReportTable_anyState_callIsProxiedToScheduleService() {

        // WHEN
        scheduleController.getFullReportTable();

        // THEN
        Mockito.verify(scheduleService).getFullReportTable();
    }
}
