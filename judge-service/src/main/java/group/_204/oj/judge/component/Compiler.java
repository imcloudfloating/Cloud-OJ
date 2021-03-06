package group._204.oj.judge.component;

import group._204.oj.judge.error.UnsupportedLanguageError;
import group._204.oj.judge.model.Compile;
import group._204.oj.judge.model.Solution;
import group._204.oj.judge.type.Language;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
class Compiler {

    @Value("${project.code-dir}")
    private String codeDir;

    private final ProcessBuilder processBuilder = new ProcessBuilder();

    private static class CompileError extends Exception {
        CompileError(String msg) {
            super(msg);
        }
    }

    /**
     * 初始化
     */
    @PostConstruct
    private void init() {
        if (!codeDir.endsWith("/")) {
            codeDir += '/';
        }
        File dir = new File(codeDir);
        if (!dir.exists()) {
            if (dir.mkdirs()) {
                log.info("Dir {} does not exist, created.", codeDir);
            } else {
                log.info("Cannot create dir {}.", codeDir);
            }
        }
    }

    /**
     * 编译
     */
    public Compile compile(Solution solution) {
        String solutionId = solution.getSolutionId();
        Integer languageId = solution.getLanguage();
        String sourceCode = solution.getSourceCode();

        String src = writeCode(solutionId, languageId, sourceCode);

        if (src.isEmpty()) {
            String err = "Compile error: Cannot write source code.";
            log.error(err);
            return new Compile(solutionId, -1, err);
        }

        try {
            Language language = Language.get(languageId);
            return compileSource(solutionId, language);
        } catch (UnsupportedLanguageError e) {
            log.error("Compile error: {}", e.getMessage());
            return new Compile(solutionId, -1, e.getMessage());
        }
    }

    /**
     * 根据语言类型编译源码
     *
     * @return {@link Compile} 编译结果
     */
    private Compile compileSource(String solutionId, Language language) throws UnsupportedLanguageError {

        if (language == null) {
            throw new UnsupportedLanguageError("null");
        }

        String solutionDir = codeDir + solutionId;

        String[] cmd;

        switch (language) {
            case C:
                cmd = new String[]{"gcc", "-std=c11", "-fmax-errors=1", "-Wfatal-errors", "Solution.c", "-o", "Solution"};
                break;
            case CPP:
                cmd = new String[]{"g++", "-std=c++17", "-fmax-errors=1", "-Wfatal-errors", "Solution.cpp", "-o", "Solution"};
                break;
            case JAVA:
                cmd = new String[]{"javac", "-encoding", "UTF-8", "Solution.java"};
                break;
            case C_SHARP:
                cmd = new String[]{"mcs", "Solution.cs"};
                break;
            case KOTLIN:
                cmd = new String[]{"kotlinc", "Solution.kt"};
                break;
            case GO:
                cmd = new String[]{"go", "build", "Solution.go"};
                break;
            case PYTHON:
            case BASH:
            case JAVA_SCRIPT:
                return new Compile(solutionId, 0);
            default:
                throw new UnsupportedLanguageError(language.toString());
        }

        try {
            processBuilder.command(cmd);
            processBuilder.directory(new File(solutionDir));

            Process process = processBuilder.start();

            if (process.waitFor(6, TimeUnit.SECONDS)) {
                if (process.exitValue() == 0) {
                    return new Compile(solutionId, 0, null);
                } else {
                    String error = IOUtils.toString(process.getErrorStream());
                    throw new CompileError(error);
                }
            } else {
                process.destroy();
                throw new InterruptedException("Compile timeout.");
            }
        } catch (IOException | InterruptedException | CompileError e) {
            log.error("Compile error: solutionId={}, error={}", solutionId, e.getMessage());
            return new Compile(solutionId, -1, e.getMessage());
        }
    }

    /**
     * 将源码写入文件
     *
     * @return 文件路径
     */
    private String writeCode(String solutionId, int language, String source) {
        File sourceFile;
        File solutionDir = new File(codeDir + solutionId);

        if (!solutionDir.mkdirs()) {
            log.error("Cannot create dir {}.", solutionDir.getName());
            return "";
        }

        sourceFile = new File(solutionDir + "/Solution" + Language.getExt(language));

        try {
            if (sourceFile.exists() || sourceFile.createNewFile()) {
                FileWriter writer = new FileWriter(sourceFile, false);
                writer.write(source);
                writer.flush();
                writer.close();

                return sourceFile.getPath();
            } else {
                log.error("Cannot creat file {}.", sourceFile.getName());
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }

        return "";
    }
}
