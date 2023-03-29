# ChangeLog

## 1.1.0-SNAPSHOT

### New Features

- None

### Changes

- None

### Bug fixes & Improvements

- None

## 1.0.0

Support for [Spring Batch 5.0.0](https://github.com/spring-projects/spring-batch/releases/tag/v5.0.0).

### New Features

- JobBuilderDsl
    - Add observationRegistry method
    - Add meterRegistry method
- StepBuilderDsl
    - Add observationRegistry method
    - Add meterRegistry method
    - Add TaskletStep, SimpleStep related methods using transactionManager

### Changes

- BatchDsl
    - Constructor now takes BeanFactory and JobRepository
- StepBuilderDsl
    - Remove transactionManager method
    - Deprecate TaskletStep, SimpleStep related methods which don't use transactionManager
- SimpleStepBuilderDsl
    - Deprecate throttleLimit method
- ItemStreamWriterDelegate
    - Change the signature of write method
- Remove deprecated adaptor classes

### Bug fixes & Improvements

- None

## 0.3.0

### New Features

- Dry run mode in DeleteMetadataJob

### Changes

- Move adapter classes to adapter package

### Bug fixes & Improvements

- None

## 0.2.0

### New Features

- Introduce DeleteMetadataJob

### Changes

- Remove one depth of Kotlin DSL

### Bug fixes & Improvements

- Apply lazy configurer

## 0.1.0

### New Features

- Introduce Kotlin DSL for builders
- Introduce single class reader-processor-writer
- Introduce ClearRunIdIncrementer

### Changes

- None

### Bug fixes & Improvements

- None
