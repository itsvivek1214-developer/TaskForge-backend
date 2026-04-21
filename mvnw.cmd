@REM ----------------------------------------------------------------------------
@REM Maven Wrapper startup batch script, version 3.2.0
@REM Licensed to the Apache Software Foundation (ASF)
@REM ----------------------------------------------------------------------------

@IF "%__MVNW_ARG0_NAME__%"=="" (SET "__MVNW_ARG0_NAME__=%~nx0")
@SET ___MVNW_INTS_CALCCASE__=
@IF "%__MVNW_ARG0_NAME__%"=="mvnw.cmd" (SET "___MVNW_INTS_CALCCASE__=true")
@IF "%__MVNW_ARG0_NAME__%"=="mvnw.CMD" (SET "___MVNW_INTS_CALCCASE__=true")
@IF "%___MVNW_INTS_CALCCASE__%"=="" (SET "___MVNW_INTS_CALCCASE__=false")

@SET MAVEN_PROJECTBASEDIR=%MAVEN_BASEDIR%
@IF NOT "%MAVEN_PROJECTBASEDIR%"=="" (GOTO endDetectBaseDir)
@SET EXEC_DIR=%CD%
@SET WDIR=%EXEC_DIR%
:findBaseDir
@IF EXIST "%WDIR%\.mvn" (SET "MAVEN_PROJECTBASEDIR=%WDIR%"&GOTO endDetectBaseDir)
@cd ..
@IF "%WDIR%"=="%CD%" (SET "MAVEN_PROJECTBASEDIR=%EXEC_DIR%"&GOTO endDetectBaseDir)
@SET "WDIR=%CD%"
@GOTO findBaseDir
:endDetectBaseDir
@cd "%EXEC_DIR%"

@IF NOT EXIST "%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar" (
  @IF NOT EXIST "%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\MavenWrapperDownloader.java" (
    @echo Downloading Maven wrapper...
    @SET MVNW_REPOURL=https://repo.maven.apache.org/maven2
    @IF NOT "%MVNW_REPOURL%"=="" (
      @SET MVNW_DOWNLOAD_URL="%MVNW_REPOURL%/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar"
    ) ELSE (
      @SET MVNW_DOWNLOAD_URL="https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar"
    )
    @IF "%MVNW_VERBOSE%"=="true" (ECHO Downloading from: %MVNW_DOWNLOAD_URL%)
    @powershell -Command "& {$WebClient = New-Object System.Net.WebClient; if ('%MVNW_USERNAME%' -ne '' -and '%MVNW_PASSWORD%' -ne '') {$WebClient.Credentials = New-Object System.Net.NetworkCredential('%MVNW_USERNAME%', '%MVNW_PASSWORD%')}; $WebClient.DownloadFile('%MVNW_DOWNLOAD_URL%', '%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar')}" || GOTO error
  )
)

@SET JAVA_EXE=%JAVA_HOME%/bin/java.exe
@IF EXIST "%JAVA_EXE%" (GOTO init)
@SET JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
@IF "%ERRORLEVEL%"=="0" (GOTO init)

@ECHO.
@ECHO Error: JAVA_HOME is not set and no 'java' command could be found in your PATH.
@ECHO.
@ECHO Please set the JAVA_HOME variable in your environment to match the
@ECHO location of your Java installation.
GOTO error

:init
@SET MAVEN_OPTS=%MAVEN_OPTS%

@SET WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain

@FOR /F "usebackq tokens=1,2 delims==" %%a IN ("%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.properties") DO (
    @IF "%%a"=="wrapperUrl" (SET "WRAPPER_JAR_URL=%%b")
)

@IF NOT EXIST "%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar" (
  @IF "%MVNW_VERBOSE%"=="true" (ECHO Downloading maven-wrapper.jar from: !WRAPPER_JAR_URL!)
  @powershell -Command "& {$WebClient = New-Object System.Net.WebClient; $WebClient.DownloadFile('!WRAPPER_JAR_URL!', '%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar')}"
)

@"%JAVA_EXE%" ^
  -classpath "%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar" ^
  "-Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECTBASEDIR%" ^
  %WRAPPER_LAUNCHER% %MAVEN_CONFIG% %*

:error
@SET ERROR_CODE=%ERRORLEVEL%
@EXIT /B %ERROR_CODE%
