package org.laoli.judge.service.compile;

import org.laoli.judge.model.enums.Language;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @Description 编译器工厂类
 * @Author laoli
 * @Date 2025/4/20 15:58
 */

@Service
public class CompilerFactory {
    Map<String,Compiler> CompilerMap;
    CompilerFactory(Map<String,Compiler> CompilerMap){
        this.CompilerMap=CompilerMap;
    }
    public Compiler getCompiler(Language language) {
        return CompilerMap.get(language.getLanguage());
    }
}
