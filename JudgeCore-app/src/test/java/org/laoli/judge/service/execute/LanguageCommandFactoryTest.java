package org.laoli.judge.service.execute;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.laoli.judge.config.SandboxConfig;
import org.laoli.judge.model.enums.Language;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @Description LanguageCommandFactory单元测试
 * @Author laoli
 * @Date 2025/4/20 15:58
 */
@ExtendWith(MockitoExtension.class)
class LanguageCommandFactoryTest {

    @Mock
    private SandboxConfig sandboxConfig;

    @Mock
    private SandboxConfig.CommonOptions commonOptions;

    @InjectMocks
    private LanguageCommandFactory factory;

    private final Path testWorkDir = Paths.get("/tmp/test_judge");

    @Test
    void testGetCommandWithSandboxEnabled() {
        when(sandboxConfig.isEnabled()).thenReturn(true);
        when(sandboxConfig.getCommand()).thenReturn("firejail");
        when(sandboxConfig.getCommonOptions()).thenReturn(commonOptions);
        when(commonOptions.isQuiet()).thenReturn(true);
        when(commonOptions.isSeccomp()).thenReturn(true);
        when(commonOptions.isNetNone()).thenReturn(true);
        when(commonOptions.isNoGroups()).thenReturn(true);
        when(commonOptions.isNoNewPrivs()).thenReturn(true);
        when(commonOptions.getCapsDrop()).thenReturn("all");

        String[] command = factory.getCommand(Language.PYTHON, testWorkDir);

        assertNotNull(command);
        assertTrue(command.length > 0);
        assertEquals("firejail", command[0]);
    }

    @Test
    void testGetCommandWithSandboxDisabled() {
        when(sandboxConfig.isEnabled()).thenReturn(false);

        String[] command = factory.getCommand(Language.PYTHON, testWorkDir);

        assertNotNull(command);
        assertTrue(command.length > 0);
        assertEquals("python3", command[0]);
    }

    @Test
    void testPythonCommandWithSandbox() {
        when(sandboxConfig.isEnabled()).thenReturn(true);
        when(sandboxConfig.getCommand()).thenReturn("firejail");
        when(sandboxConfig.getCommonOptions()).thenReturn(commonOptions);
        when(commonOptions.isQuiet()).thenReturn(true);
        when(commonOptions.isSeccomp()).thenReturn(true);
        when(commonOptions.isNetNone()).thenReturn(true);
        when(commonOptions.isNoGroups()).thenReturn(true);
        when(commonOptions.isNoNewPrivs()).thenReturn(true);
        when(commonOptions.getCapsDrop()).thenReturn("all");

        String[] command = factory.getCommand(Language.PYTHON, testWorkDir);

        assertTrue(command.length > 0);
        assertTrue(String.join(" ", command).contains("python3"));
    }

    @Test
    void testJavaCommandWithSandbox() {
        when(sandboxConfig.isEnabled()).thenReturn(true);
        when(sandboxConfig.getCommand()).thenReturn("firejail");
        when(sandboxConfig.getCommonOptions()).thenReturn(commonOptions);
        when(commonOptions.isQuiet()).thenReturn(true);
        when(commonOptions.isSeccomp()).thenReturn(true);
        when(commonOptions.isNetNone()).thenReturn(true);
        when(commonOptions.isNoGroups()).thenReturn(true);
        when(commonOptions.isNoNewPrivs()).thenReturn(true);
        when(commonOptions.getCapsDrop()).thenReturn("all");

        String[] command = factory.getCommand(Language.JAVA, testWorkDir);

        assertTrue(command.length > 0);
        assertTrue(String.join(" ", command).contains("java"));
    }

    @Test
    void testCppCommandWithSandbox() {
        when(sandboxConfig.isEnabled()).thenReturn(true);
        when(sandboxConfig.getCommand()).thenReturn("firejail");
        when(sandboxConfig.getCommonOptions()).thenReturn(commonOptions);
        when(commonOptions.isQuiet()).thenReturn(true);
        when(commonOptions.isSeccomp()).thenReturn(true);
        when(commonOptions.isNetNone()).thenReturn(true);
        when(commonOptions.isNoGroups()).thenReturn(true);
        when(commonOptions.isNoNewPrivs()).thenReturn(true);
        when(commonOptions.getCapsDrop()).thenReturn("all");

        String[] command = factory.getCommand(Language.CPP, testWorkDir);

        assertTrue(command.length > 0);
        assertTrue(String.join(" ", command).contains("cpp_solution"));
    }

    @Test
    void testCCommandWithSandbox() {
        when(sandboxConfig.isEnabled()).thenReturn(true);
        when(sandboxConfig.getCommand()).thenReturn("firejail");
        when(sandboxConfig.getCommonOptions()).thenReturn(commonOptions);
        when(commonOptions.isQuiet()).thenReturn(true);
        when(commonOptions.isSeccomp()).thenReturn(true);
        when(commonOptions.isNetNone()).thenReturn(true);
        when(commonOptions.isNoGroups()).thenReturn(true);
        when(commonOptions.isNoNewPrivs()).thenReturn(true);
        when(commonOptions.getCapsDrop()).thenReturn("all");

        String[] command = factory.getCommand(Language.C, testWorkDir);

        assertTrue(command.length > 0);
        assertTrue(String.join(" ", command).contains("c_solution"));
    }

    @Test
    void testRustCommandWithSandbox() {
        when(sandboxConfig.isEnabled()).thenReturn(true);
        when(sandboxConfig.getCommand()).thenReturn("firejail");
        when(sandboxConfig.getCommonOptions()).thenReturn(commonOptions);
        when(commonOptions.isQuiet()).thenReturn(true);
        when(commonOptions.isSeccomp()).thenReturn(true);
        when(commonOptions.isNetNone()).thenReturn(true);
        when(commonOptions.isNoGroups()).thenReturn(true);
        when(commonOptions.isNoNewPrivs()).thenReturn(true);
        when(commonOptions.getCapsDrop()).thenReturn("all");

        String[] command = factory.getCommand(Language.RUST, testWorkDir);

        assertTrue(command.length > 0);
        assertTrue(String.join(" ", command).contains("rust_solution"));
    }

    @Test
    void testGoCommandWithSandbox() {
        when(sandboxConfig.isEnabled()).thenReturn(true);
        when(sandboxConfig.getCommand()).thenReturn("firejail");
        when(sandboxConfig.getCommonOptions()).thenReturn(commonOptions);
        when(commonOptions.isQuiet()).thenReturn(true);
        when(commonOptions.isSeccomp()).thenReturn(true);
        when(commonOptions.isNetNone()).thenReturn(true);
        when(commonOptions.isNoGroups()).thenReturn(true);
        when(commonOptions.isNoNewPrivs()).thenReturn(true);
        when(commonOptions.getCapsDrop()).thenReturn("all");

        String[] command = factory.getCommand(Language.GO, testWorkDir);

        assertTrue(command.length > 0);
        assertTrue(String.join(" ", command).contains("go_solution"));
    }

    @Test
    void testPhpCommandWithSandbox() {
        when(sandboxConfig.isEnabled()).thenReturn(true);
        when(sandboxConfig.getCommand()).thenReturn("firejail");
        when(sandboxConfig.getCommonOptions()).thenReturn(commonOptions);
        when(commonOptions.isQuiet()).thenReturn(true);
        when(commonOptions.isSeccomp()).thenReturn(true);
        when(commonOptions.isNetNone()).thenReturn(true);
        when(commonOptions.isNoGroups()).thenReturn(true);
        when(commonOptions.isNoNewPrivs()).thenReturn(true);
        when(commonOptions.getCapsDrop()).thenReturn("all");

        String[] command = factory.getCommand(Language.PHP, testWorkDir);

        assertTrue(command.length > 0);
        assertTrue(String.join(" ", command).contains("php"));
    }

    @Test
    void testUnsupportedLanguage() {
        String[] command = factory.getCommand(null, testWorkDir);

        assertNotNull(command);
        assertEquals(0, command.length);
    }

    @Test
    void testIsSandboxEnabled() {
        when(sandboxConfig.isEnabled()).thenReturn(true);

        assertTrue(factory.isSandboxEnabled());

        when(sandboxConfig.isEnabled()).thenReturn(false);

        assertFalse(factory.isSandboxEnabled());
    }
}
