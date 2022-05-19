import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import ru.netology.patient.entity.BloodPressure;
import ru.netology.patient.entity.HealthInfo;
import ru.netology.patient.entity.PatientInfo;
import ru.netology.patient.repository.PatientInfoRepository;
import ru.netology.patient.service.alert.SendAlertService;
import ru.netology.patient.service.alert.SendAlertServiceImpl;
import ru.netology.patient.service.medical.MedicalService;
import ru.netology.patient.service.medical.MedicalServiceImpl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MedicalServiceImplTests {
    private String expected = String.format("Warning, patient with id: %s, need help", null);
    private PatientInfoRepository patientInfoRepository = Mockito.mock(PatientInfoRepository.class);
    private SendAlertService sendAlertService = Mockito.mock(SendAlertService.class);
    private MedicalService medicalService = new MedicalServiceImpl(patientInfoRepository, sendAlertService);
    private PatientInfo patient = new PatientInfo("Иван", "Петров", LocalDate.of(1980, 11, 26),
            new HealthInfo(new BigDecimal("36.65"), new BloodPressure(120, 80)));
    private ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);

    @BeforeEach
    public void init() {
        System.out.println("test started");
        Mockito.when(patientInfoRepository.getById(Mockito.anyString())).thenReturn(patient);
    }

    @BeforeAll
    public static void started() {
        System.out.println("tests started");
    }

    @AfterEach
    public void finished() {
        System.out.println("\ntest compiled");
    }

    @AfterAll
    public static void finishedAll() {
        System.out.println("tests finished");
    }

    @ParameterizedTest
    @MethodSource("sourceBloodPressure")
    public void testCheckBloodPressure(int high, int low) {
        //arrange
        medicalService.checkBloodPressure("", new BloodPressure(high, low));
        //act
        Mockito.verify(sendAlertService).send(argumentCaptor.capture());
        //assert
        Mockito.verify(patientInfoRepository, Mockito.times(1)).getById(Mockito.anyString());
        Mockito.verify(sendAlertService, Mockito.only()).send(expected);
        assertEquals(expected, argumentCaptor.getValue());
    }

    @Test
    public void testCheckBloodPressureIndicatorsAreNormal() {
        medicalService.checkBloodPressure("", new BloodPressure(120, 80));
        Mockito.verify(sendAlertService, Mockito.times(0)).send(expected);
    }

    @ParameterizedTest
    @MethodSource("sourceTemperature")
    public void testCheckTemperature(BigDecimal bigDecimal) {
        //arrange
        medicalService.checkTemperature("", bigDecimal);
        //act
        Mockito.verify(sendAlertService).send(argumentCaptor.capture());
        //assert
        Mockito.verify(patientInfoRepository, Mockito.times(1)).getById(Mockito.anyString());
        Mockito.verify(sendAlertService, Mockito.only()).send(expected);
        assertEquals(expected, argumentCaptor.getValue());
    }

    @Test
    public void testCheckTemperatureIndicatorsAreNormal() {
        medicalService.checkTemperature("", new BigDecimal("36.65"));
        Mockito.verify(sendAlertService, Mockito.times(0)).send(expected);
    }

    private static Stream<Arguments> sourceBloodPressure() {
        return Stream.of(Arguments.of(110, 90),
                Arguments.of(130, 70),
                Arguments.of(150, 50));
    }

    private static Stream<Arguments> sourceTemperature() {
        return Stream.of(Arguments.of(new BigDecimal("32.3")),
                Arguments.of(new BigDecimal("33.3")),
                Arguments.of(new BigDecimal("34.4")));
    }
}
