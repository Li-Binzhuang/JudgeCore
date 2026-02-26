package org.laoli.judge.service.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.laoli.judge.model.dto.CodeTestRequest;
import org.laoli.judge.model.dto.CodeTestRequest.CodeTestCase;
import org.laoli.judge.model.dto.CodeTestResponse;
import org.laoli.judge.model.dto.CodeTestResponse.TestStatus;
import org.laoli.judge.model.entity.CaseResult;
import org.laoli.judge.model.enums.Language;
import org.laoli.judge.model.enums.SimpleResult;
import org.laoli.judge.service.compile.Compiler;
import org.laoli.judge.service.compile.CompilerFactory;
import org.laoli.judge.service.execute.CodeExecutor;
import org.laoli.judge.service.execute.LanguageCommandFactory;
import org.laoli.judge.service.validation.InputValidator;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CodeTestService Tests")
class CodeTestServiceTest {

        @Mock
        private CodeExecutor executor;

        @Mock
        private LanguageCommandFactory languageCommandFactory;

        @Mock
        private CompilerFactory compilerFactory;

        @Mock
        private InputValidator inputValidator;

        @InjectMocks
        private org.laoli.judge.service.test.impl.CodeTestService codeTestService;

        private static final String VALID_CODE = "public class Main { public static void main(String[] args) { } }";
        private static final Language JAVA = Language.JAVA;
        private static final long DEFAULT_TIME_LIMIT = 1000L;
        private static final long DEFAULT_MEMORY_LIMIT = 4096L;

        @Nested
        @DisplayName("Code Execution Tests (No Test Cases)")
        class CodeExecutionTests {

                @Test
                @DisplayName("Should execute code without test cases")
                void shouldExecuteCodeWithoutTestCases() throws Exception {
                        CodeTestRequest request = CodeTestRequest.builder()
                                        .code(VALID_CODE)
                                        .language("JAVA")
                                        .testCases(null)
                                        .timeLimit(DEFAULT_TIME_LIMIT)
                                        .memoryLimit(DEFAULT_MEMORY_LIMIT)
                                        .showDetail(true)
                                        .build();

                        when(compilerFactory.getCompiler(JAVA)).thenReturn(mock(Compiler.class));
                        when(languageCommandFactory.getCommand(eq(JAVA), any()))
                                        .thenReturn(new String[] { "java", "Main" });

                        CaseResult execResult = CaseResult.builder()
                                        .status(SimpleResult.ACCEPTED)
                                        .executionTime(50L)
                                        .memoryUsed(1024L)
                                        .actualOutput("Hello World")
                                        .build();
                        when(executor.execute(any(), any(), any(), anyLong(), anyLong())).thenReturn(execResult);

                        CodeTestResponse response = codeTestService.executeTest(request);

                        assertNotNull(response);
                        assertEquals(TestStatus.EXECUTED_ONLY, response.getStatus());
                        assertEquals(50L, response.getTotalExecutionTime());
                }

                @Test
                @DisplayName("Should execute code with empty test cases list")
                void shouldExecuteCodeWithEmptyTestCasesList() throws Exception {
                        CodeTestRequest request = CodeTestRequest.builder()
                                        .code(VALID_CODE)
                                        .language("JAVA")
                                        .testCases(Collections.emptyList())
                                        .timeLimit(DEFAULT_TIME_LIMIT)
                                        .memoryLimit(DEFAULT_MEMORY_LIMIT)
                                        .showDetail(true)
                                        .build();

                        when(compilerFactory.getCompiler(JAVA)).thenReturn(mock(Compiler.class));
                        when(languageCommandFactory.getCommand(eq(JAVA), any()))
                                        .thenReturn(new String[] { "java", "Main" });

                        CaseResult execResult = CaseResult.builder()
                                        .status(SimpleResult.ACCEPTED)
                                        .executionTime(50L)
                                        .memoryUsed(1024L)
                                        .actualOutput("Output")
                                        .build();
                        when(executor.execute(any(), any(), any(), anyLong(), anyLong())).thenReturn(execResult);

                        CodeTestResponse response = codeTestService.executeTest(request);

                        assertNotNull(response);
                        assertEquals(TestStatus.EXECUTED_ONLY, response.getStatus());
                }
        }

        @Nested
        @DisplayName("Test Case Execution Tests")
        class TestCaseExecutionTests {

                @Test
                @DisplayName("Should return ALL_PASSED when all test cases pass")
                void shouldReturnAllPassedWhenAllTestCasesPass() throws Exception {
                        CodeTestRequest request = CodeTestRequest.builder()
                                        .code(VALID_CODE)
                                        .language("JAVA")
                                        .testCases(List.of(
                                                        CodeTestCase.builder()
                                                                        .id("case1")
                                                                        .input("1 2")
                                                                        .expectedOutput("3")
                                                                        .description("Test case 1")
                                                                        .build(),
                                                        CodeTestCase.builder()
                                                                        .id("case2")
                                                                        .input("3 4")
                                                                        .expectedOutput("7")
                                                                        .description("Test case 2")
                                                                        .build()))
                                        .timeLimit(DEFAULT_TIME_LIMIT)
                                        .memoryLimit(DEFAULT_MEMORY_LIMIT)
                                        .showDetail(true)
                                        .build();

                        when(compilerFactory.getCompiler(JAVA)).thenReturn(mock(Compiler.class));
                        when(languageCommandFactory.getCommand(eq(JAVA), any()))
                                        .thenReturn(new String[] { "java", "Main" });

                        CaseResult passResult = CaseResult.builder()
                                        .status(SimpleResult.ACCEPTED)
                                        .executionTime(50L)
                                        .memoryUsed(1024L)
                                        .actualOutput("3")
                                        .build();
                        when(executor.execute(any(), any(), any(), anyLong(), anyLong())).thenReturn(passResult);

                        CodeTestResponse response = codeTestService.executeTest(request);

                        assertNotNull(response);
                        assertEquals(TestStatus.ALL_PASSED, response.getStatus());
                        assertEquals(2, response.getTotalCount());
                        assertEquals(2, response.getPassedCount());
                }

                @Test
                @DisplayName("Should return PARTIAL_PASSED when some test cases pass")
                void shouldReturnPartialPassedWhenSomeTestCasesPass() throws Exception {
                        CodeTestRequest request = CodeTestRequest.builder()
                                        .code(VALID_CODE)
                                        .language("JAVA")
                                        .testCases(List.of(
                                                        CodeTestCase.builder()
                                                                        .id("case1")
                                                                        .input("1 2")
                                                                        .expectedOutput("3")
                                                                        .build(),
                                                        CodeTestCase.builder()
                                                                        .id("case2")
                                                                        .input("3 4")
                                                                        .expectedOutput("7")
                                                                        .build()))
                                        .timeLimit(DEFAULT_TIME_LIMIT)
                                        .memoryLimit(DEFAULT_MEMORY_LIMIT)
                                        .showDetail(false)
                                        .build();

                        when(compilerFactory.getCompiler(JAVA)).thenReturn(mock(Compiler.class));
                        when(languageCommandFactory.getCommand(eq(JAVA), any()))
                                        .thenReturn(new String[] { "java", "Main" });

                        // First test passes, second fails
                        CaseResult passResult = CaseResult.builder()
                                        .status(SimpleResult.ACCEPTED)
                                        .executionTime(50L)
                                        .memoryUsed(1024L)
                                        .actualOutput("3")
                                        .build();

                        CaseResult failResult = CaseResult.builder()
                                        .status(SimpleResult.WRONG_ANSWER)
                                        .executionTime(50L)
                                        .memoryUsed(1024L)
                                        .actualOutput("wrong")
                                        .build();

                        when(executor.execute(any(), any(), any(), anyLong(), anyLong()))
                                        .thenReturn(passResult)
                                        .thenReturn(failResult);

                        CodeTestResponse response = codeTestService.executeTest(request);

                        assertNotNull(response);
                        assertEquals(TestStatus.PARTIAL_PASSED, response.getStatus());
                        assertEquals(1, response.getPassedCount());
                        assertEquals(2, response.getTotalCount());
                }

                @Test
                @DisplayName("Should return ALL_FAILED when all test cases fail")
                void shouldReturnAllFailedWhenAllTestCasesFail() throws Exception {
                        CodeTestRequest request = CodeTestRequest.builder()
                                        .code(VALID_CODE)
                                        .language("JAVA")
                                        .testCases(List.of(
                                                        CodeTestCase.builder()
                                                                        .id("case1")
                                                                        .input("1 2")
                                                                        .expectedOutput("3")
                                                                        .build()))
                                        .timeLimit(DEFAULT_TIME_LIMIT)
                                        .memoryLimit(DEFAULT_MEMORY_LIMIT)
                                        .showDetail(false)
                                        .build();

                        when(compilerFactory.getCompiler(JAVA)).thenReturn(mock(Compiler.class));
                        when(languageCommandFactory.getCommand(eq(JAVA), any()))
                                        .thenReturn(new String[] { "java", "Main" });

                        CaseResult failResult = CaseResult.builder()
                                        .status(SimpleResult.WRONG_ANSWER)
                                        .executionTime(50L)
                                        .memoryUsed(1024L)
                                        .actualOutput("wrong")
                                        .build();
                        when(executor.execute(any(), any(), any(), anyLong(), anyLong())).thenReturn(failResult);

                        CodeTestResponse response = codeTestService.executeTest(request);

                        assertNotNull(response);
                        assertEquals(TestStatus.ALL_FAILED, response.getStatus());
                        assertEquals(0, response.getPassedCount());
                        assertEquals(1, response.getTotalCount());
                }
        }

        @Nested
        @DisplayName("Error Handling Tests")
        class ErrorHandlingTests {

                @Test
                @DisplayName("Should return COMPILE_ERROR when compilation fails")
                void shouldReturnCompileErrorWhenCompilationFails() throws Exception {
                        CodeTestRequest request = CodeTestRequest.builder()
                                        .code("invalid code")
                                        .language("JAVA")
                                        .testCases(List.of(
                                                        CodeTestCase.builder()
                                                                        .input("test")
                                                                        .expectedOutput("test")
                                                                        .build()))
                                        .timeLimit(DEFAULT_TIME_LIMIT)
                                        .memoryLimit(DEFAULT_MEMORY_LIMIT)
                                        .build();

                        Compiler mockCompiler = mock(Compiler.class);
                        when(compilerFactory.getCompiler(JAVA)).thenReturn(mockCompiler);

                        var compileError = org.laoli.judge.model.aggregate.JudgeResult.builder()
                                        .status(SimpleResult.COMPILATION_ERROR)
                                        .message("Syntax error")
                                        .executionTime(0)
                                        .memoryUsed(0)
                                        .build();
                        when(mockCompiler.compile(anyString(), any())).thenReturn(compileError);

                        CodeTestResponse response = codeTestService.executeTest(request);

                        assertNotNull(response);
                        assertEquals(TestStatus.COMPILE_ERROR, response.getStatus());
                }

                @Test
                @DisplayName("Should return RUNTIME_ERROR when execution throws exception")
                void shouldReturnRuntimeErrorWhenExecutionThrowsException() throws Exception {
                        CodeTestRequest request = CodeTestRequest.builder()
                                        .code(VALID_CODE)
                                        .language("JAVA")
                                        .testCases(List.of(
                                                        CodeTestCase.builder()
                                                                        .input("test")
                                                                        .expectedOutput("test")
                                                                        .build()))
                                        .timeLimit(DEFAULT_TIME_LIMIT)
                                        .memoryLimit(DEFAULT_MEMORY_LIMIT)
                                        .build();

                        when(compilerFactory.getCompiler(JAVA)).thenReturn(mock(Compiler.class));
                        when(languageCommandFactory.getCommand(eq(JAVA), any()))
                                        .thenReturn(new String[] { "java", "Main" });

                        CaseResult runtimeError = CaseResult.builder()
                                        .status(SimpleResult.RUNTIME_ERROR)
                                        .message("NullPointerException")
                                        .executionTime(50L)
                                        .memoryUsed(1024L)
                                        .build();
                        when(executor.execute(any(), any(), any(), anyLong(), anyLong())).thenReturn(runtimeError);

                        CodeTestResponse response = codeTestService.executeTest(request);

                        assertNotNull(response);
                        assertEquals(TestStatus.RUNTIME_ERROR, response.getStatus());
                }

                @Test
                @DisplayName("Should return TIME_LIMIT_EXCEEDED when execution times out")
                void shouldReturnTimeLimitExceededWhenExecutionTimesOut() throws Exception {
                        CodeTestRequest request = CodeTestRequest.builder()
                                        .code(VALID_CODE)
                                        .language("JAVA")
                                        .testCases(List.of(
                                                        CodeTestCase.builder()
                                                                        .input("test")
                                                                        .expectedOutput("test")
                                                                        .build()))
                                        .timeLimit(DEFAULT_TIME_LIMIT)
                                        .memoryLimit(DEFAULT_MEMORY_LIMIT)
                                        .build();

                        when(compilerFactory.getCompiler(JAVA)).thenReturn(mock(Compiler.class));
                        when(languageCommandFactory.getCommand(eq(JAVA), any()))
                                        .thenReturn(new String[] { "java", "Main" });

                        CaseResult timeoutResult = CaseResult.builder()
                                        .status(SimpleResult.TIME_LIMIT_EXCEEDED)
                                        .executionTime(DEFAULT_TIME_LIMIT + 100)
                                        .memoryUsed(1024L)
                                        .build();
                        when(executor.execute(any(), any(), any(), anyLong(), anyLong())).thenReturn(timeoutResult);

                        CodeTestResponse response = codeTestService.executeTest(request);

                        assertNotNull(response);
                        assertEquals(TestStatus.TIME_LIMIT_EXCEEDED, response.getStatus());
                }

                @Test
                @DisplayName("Should return SYSTEM_ERROR for invalid language")
                void shouldReturnSystemErrorForInvalidLanguage() {
                        CodeTestRequest request = CodeTestRequest.builder()
                                        .code(VALID_CODE)
                                        .language("INVALID_LANGUAGE")
                                        .testCases(Collections.emptyList())
                                        .timeLimit(DEFAULT_TIME_LIMIT)
                                        .memoryLimit(DEFAULT_MEMORY_LIMIT)
                                        .build();

                        CodeTestResponse response = codeTestService.executeTest(request);

                        assertNotNull(response);
                        assertEquals(TestStatus.SYSTEM_ERROR, response.getStatus());
                }

                @Test
                @DisplayName("Should return SYSTEM_ERROR for empty code")
                void shouldReturnSystemErrorForEmptyCode() {
                        CodeTestRequest request = CodeTestRequest.builder()
                                        .code("")
                                        .language("JAVA")
                                        .testCases(Collections.emptyList())
                                        .timeLimit(DEFAULT_TIME_LIMIT)
                                        .memoryLimit(DEFAULT_MEMORY_LIMIT)
                                        .build();

                        CodeTestResponse response = codeTestService.executeTest(request);

                        assertNotNull(response);
                        assertEquals(TestStatus.SYSTEM_ERROR, response.getStatus());
                }
        }

        @Nested
        @DisplayName("Single Test Execution Tests")
        class SingleTestExecutionTests {

                @Test
                @DisplayName("Should execute single test case")
                void shouldExecuteSingleTestCase() throws Exception {
                        CodeTestRequest request = CodeTestRequest.builder()
                                        .code(VALID_CODE)
                                        .language("JAVA")
                                        .testCases(List.of(
                                                        CodeTestCase.builder()
                                                                        .id("single_case")
                                                                        .input("1 2")
                                                                        .expectedOutput("3")
                                                                        .build()))
                                        .timeLimit(DEFAULT_TIME_LIMIT)
                                        .memoryLimit(DEFAULT_MEMORY_LIMIT)
                                        .showDetail(true)
                                        .build();

                        when(compilerFactory.getCompiler(JAVA)).thenReturn(mock(Compiler.class));
                        when(languageCommandFactory.getCommand(eq(JAVA), any()))
                                        .thenReturn(new String[] { "java", "Main" });

                        CaseResult passResult = CaseResult.builder()
                                        .status(SimpleResult.ACCEPTED)
                                        .executionTime(50L)
                                        .memoryUsed(1024L)
                                        .actualOutput("3")
                                        .build();
                        when(executor.execute(any(), any(), any(), anyLong(), anyLong())).thenReturn(passResult);

                        CodeTestResponse response = codeTestService.executeSingleTest(request);

                        assertNotNull(response);
                        assertEquals(TestStatus.ALL_PASSED, response.getStatus());
                        assertEquals(1, response.getTotalCount());
                }
        }
}
