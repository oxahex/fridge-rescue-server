package team.rescue.validator;

import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;

@Component
@RequiredArgsConstructor
public class ListValidator implements Validator {

	private final SpringValidatorAdapter validator;

	@Override
	public boolean supports(Class<?> clazz) {
		return List.class.equals(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		for (Object object : (Collection) target) {
			validator.validate(object, errors);
		}

	}
}
