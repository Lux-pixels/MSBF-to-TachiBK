package validation

/**
 * Result of one validation step.
 *
 * Errors block conversion.
 * Warnings are shown to the user but do not block conversion.
 */
data class ValidationResult(
    val errors: List<String> = emptyList(),
    val warnings: List<String> = emptyList(),
) {
    val isValid: Boolean
        get() = errors.isEmpty()

    operator fun plus(other: ValidationResult): ValidationResult {
        return ValidationResult(
            errors = errors + other.errors,
            warnings = warnings + other.warnings,
        )
    }

    companion object {
        fun ok(): ValidationResult = ValidationResult()
    }
}