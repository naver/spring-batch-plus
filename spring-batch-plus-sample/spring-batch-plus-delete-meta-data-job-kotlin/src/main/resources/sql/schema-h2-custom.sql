CREATE TABLE CUSTOM_JOB_INSTANCE  (
	JOB_INSTANCE_ID BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY ,
	VERSION BIGINT ,
	JOB_NAME VARCHAR(100) NOT NULL,
	JOB_KEY VARCHAR(32) NOT NULL,
	constraint JOB_INST_UN unique (JOB_NAME, JOB_KEY)
) ;

CREATE TABLE CUSTOM_JOB_EXECUTION  (
	JOB_EXECUTION_ID BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY ,
	VERSION BIGINT  ,
	JOB_INSTANCE_ID BIGINT NOT NULL,
	CREATE_TIME TIMESTAMP(9) NOT NULL,
	START_TIME TIMESTAMP(9) DEFAULT NULL ,
	END_TIME TIMESTAMP(9) DEFAULT NULL ,
	STATUS VARCHAR(10) ,
	EXIT_CODE VARCHAR(2500) ,
	EXIT_MESSAGE VARCHAR(2500) ,
	LAST_UPDATED TIMESTAMP(9),
	constraint JOB_INST_EXEC_FK foreign key (JOB_INSTANCE_ID)
	references CUSTOM_JOB_INSTANCE(JOB_INSTANCE_ID)
) ;

CREATE TABLE CUSTOM_JOB_EXECUTION_PARAMS  (
	JOB_EXECUTION_ID BIGINT NOT NULL ,
	PARAMETER_NAME VARCHAR(100) NOT NULL ,
	PARAMETER_TYPE VARCHAR(100) NOT NULL ,
	PARAMETER_VALUE VARCHAR(2500) ,
	IDENTIFYING CHAR(1) NOT NULL ,
	constraint JOB_EXEC_PARAMS_FK foreign key (JOB_EXECUTION_ID)
	references CUSTOM_JOB_EXECUTION(JOB_EXECUTION_ID)
) ;

CREATE TABLE CUSTOM_STEP_EXECUTION  (
	STEP_EXECUTION_ID BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY ,
	VERSION BIGINT NOT NULL,
	STEP_NAME VARCHAR(100) NOT NULL,
	JOB_EXECUTION_ID BIGINT NOT NULL,
	CREATE_TIME TIMESTAMP(9) NOT NULL,
	START_TIME TIMESTAMP(9) DEFAULT NULL ,
	END_TIME TIMESTAMP(9) DEFAULT NULL ,
	STATUS VARCHAR(10) ,
	COMMIT_COUNT BIGINT ,
	READ_COUNT BIGINT ,
	FILTER_COUNT BIGINT ,
	WRITE_COUNT BIGINT ,
	READ_SKIP_COUNT BIGINT ,
	WRITE_SKIP_COUNT BIGINT ,
	PROCESS_SKIP_COUNT BIGINT ,
	ROLLBACK_COUNT BIGINT ,
	EXIT_CODE VARCHAR(2500) ,
	EXIT_MESSAGE VARCHAR(2500) ,
	LAST_UPDATED TIMESTAMP(9),
	constraint JOB_EXEC_STEP_FK foreign key (JOB_EXECUTION_ID)
	references CUSTOM_JOB_EXECUTION(JOB_EXECUTION_ID)
) ;

CREATE TABLE CUSTOM_STEP_EXECUTION_CONTEXT  (
	STEP_EXECUTION_ID BIGINT NOT NULL PRIMARY KEY,
	SHORT_CONTEXT VARCHAR(2500) NOT NULL,
	SERIALIZED_CONTEXT LONGVARCHAR ,
	constraint STEP_EXEC_CTX_FK foreign key (STEP_EXECUTION_ID)
	references CUSTOM_STEP_EXECUTION(STEP_EXECUTION_ID)
) ;

CREATE TABLE CUSTOM_JOB_EXECUTION_CONTEXT  (
	JOB_EXECUTION_ID BIGINT NOT NULL PRIMARY KEY,
	SHORT_CONTEXT VARCHAR(2500) NOT NULL,
	SERIALIZED_CONTEXT LONGVARCHAR ,
	constraint JOB_EXEC_CTX_FK foreign key (JOB_EXECUTION_ID)
	references CUSTOM_JOB_EXECUTION(JOB_EXECUTION_ID)
) ;

CREATE SEQUENCE CUSTOM_STEP_EXECUTION_SEQ;
CREATE SEQUENCE CUSTOM_JOB_EXECUTION_SEQ;
CREATE SEQUENCE CUSTOM_JOB_SEQ;
