# ChangeLog

- [1.2.x](#12x)
  - [1.2.0](#120)
- [1.1.x](#11x)
  - [1.1.0](#110)
- [1.0.x](#10x)
  - [1.0.1](#101)
  - [1.0.0](#100)
- [0.3.x](#03x)
  - [0.3.1](#031)
  - [0.3.0](#030)
- [0.2.x](#02x)
  - [0.2.0](#020)
- [0.1.x](#01x)
  - [0.1.0](#010)

## 1.2.x

### 1.2.0

Support for Spring Batch 5.2.x

#### New Features

- Add adapter for reader processor pair

#### Changes

- Bump up minimum kotlin compatibility to 1.6

#### Bug fixes & Improvements

- Remove usage of StepExecutionListenerSupport

## 1.1.x

### 1.1.0

Support for Spring Batch 5.1.x

#### New Features

- Add BatchJobObservationConvention method to JobBuilderDsl
- Add BatchStepObservationConvention method to StepBuilderDsl
- Add Iterable based adapters (ItemStreamIterableReaderProcessorWriter, ItemStreamIterableReaderWriter)
- Add Iterator based adapters (ItemStreamIteratorReaderProcessorWriter, ItemStreamIteratorReaderWriter)
- Add simple adapters (ItemStreamSimpleReaderProcessorWriter, ItemStreamSimpleReaderWriter)

#### Changes

- Deprecate repository methods in JobBuilderDsl, StepBuilderDsl
- Rename ItemStreamReaderProcessorWriter, ItemStreamReaderWriter to ItemStreamFluxReaderProcessorWriter, ItemStreamFluxReaderWriter.

#### Bug fixes & Improvements

- None

## 1.0.x

### 1.0.1

#### New Features

- None

#### Changes

- None

#### Bug fixes & Improvements

- Fix not setting started flag when decider is first

### 1.0.0

Support for Spring Batch 5.0.x

#### New Features

- JobBuilderDsl
    - Add observationRegistry method
    - Add meterRegistry method
- StepBuilderDsl
    - Add observationRegistry method
    - Add meterRegistry method
    - Add TaskletStep, SimpleStep related methods using transactionManager

#### Changes

- BatchDsl
    - Constructor now takes BeanFactory and JobRepository
- StepBuilderDsl
    - Remove transactionManager method
    - Deprecate TaskletStep, SimpleStep related methods which don't use transactionManager
- SimpleStepBuilderDsl
    - Deprecate throttleLimit method
- ItemStreamWriterDelegate
    - Change the signature of write method
- Remove deprecated adapter classes

#### Bug fixes & Improvements

- None

## 0.3.x

### 0.3.1

#### New Features

- None

#### Changes

- None

#### Bug fixes & Improvements

- Fix not setting started flag when decider is first

### 0.3.0

#### New Features

- Dry run mode in DeleteMetadataJob

#### Changes

- Move adapter classes to adapter package

#### Bug fixes & Improvements

- None

## 0.2.x

### 0.2.0

#### New Features

- Introduce DeleteMetadataJob

#### Changes

- Remove one depth of Kotlin DSL

#### Bug fixes & Improvements

- Apply lazy configurer

## 0.1.x

### 0.1.0

#### New Features

- Introduce Kotlin DSL for builders
- Introduce single class reader-processor-writer
- Introduce ClearRunIdIncrementer

#### Changes

- None

#### Bug fixes & Improvements

- None
