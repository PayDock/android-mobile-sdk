# Consumer ProGuard rules for bin-processor module
# These rules are automatically applied to consuming projects

# Keep BinDataResponse and related DTOs for serialization
-keep class com.paydock.binprocessor.data.dto.** { *; }

# Keep domain models
-keep class com.paydock.binprocessor.domain.model.** { *; }
