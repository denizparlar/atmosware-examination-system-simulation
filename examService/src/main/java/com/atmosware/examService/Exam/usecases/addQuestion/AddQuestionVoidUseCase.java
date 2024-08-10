package com.atmosware.examService.Exam.usecases.addQuestion;

import com.atmosware.common.exam.GetQuestionAndOption;
import com.atmosware.core.services.JwtService;
import com.atmosware.examService.Exam.Exam;
import com.atmosware.examService.Exam.ExamBusinessRules;
import com.atmosware.examService.Exam.ExamRepository;
import com.atmosware.examService.Exam.QuestionClient;
import com.atmosware.examService.usecase.VoidUseCase;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AddQuestionVoidUseCase implements VoidUseCase<AddQuestionUseCaseInput> {

    private final QuestionClient questionClient;
    private final ExamRepository examRepository;
    private final ExamBusinessRules examBusinessRules;
    private final JwtService jwtService;

    @Override
    public void execute(AddQuestionUseCaseInput input, HttpServletRequest request) {
        Exam exam = this.examBusinessRules.checkExamIsAlreadyStarted(input.getAddQuestionRequest().getExamId());

        String token = extractJwtFromRequest(request);
        String roleName = this.jwtService.extractRoles(token);
        String userId = this.jwtService.extractUserId(token);

        this.examBusinessRules.checkRequestRole(roleName, exam, userId);

        GetQuestionAndOption getQuestionAndOption = this.questionClient.
                getQuestionAndOption(input.getAddQuestionRequest().getQuestionId());

        assert exam != null;
        exam.getQuestionAndOptions().add(getQuestionAndOption);

        this.examRepository.save(exam);
    }

    public String extractJwtFromRequest(HttpServletRequest request) {

        String bearerToken = request.getHeader("Authorization");

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        return null;
    }
}
