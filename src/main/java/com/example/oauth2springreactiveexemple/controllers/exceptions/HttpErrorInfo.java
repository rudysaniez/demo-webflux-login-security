package com.example.oauth2springreactiveexemple.controllers.exceptions;

import java.time.LocalDateTime;
import java.util.Objects;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Information returned when an error occurs.
 */
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2021-03-26T11:12:24.549137+01:00[Europe/Paris]")
public class HttpErrorInfo   {
  @JsonProperty("timestamp")
  @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME)
  private LocalDateTime timestamp;

  @JsonProperty("path")
  private String path;

  @JsonProperty("httpStatus")
  private String httpStatus;

  @JsonProperty("message")
  private String message;

  public HttpErrorInfo timestamp(LocalDateTime timestamp) {
    this.timestamp = timestamp;
    return this;
  }

  /**
   * Temporal information.
   * @return timestamp
  */
  @NotNull
  @Valid
  public LocalDateTime getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(LocalDateTime timestamp) {
    this.timestamp = timestamp;
  }

  public HttpErrorInfo path(String path) {
    this.path = path;
    return this;
  }

  /**
   * The path of the resource using.
   * @return path
  */
  @NotNull
  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public HttpErrorInfo httpStatus(String httpStatus) {
    this.httpStatus = httpStatus;
    return this;
  }

  /**
   * Http status.
   * @return httpStatus
  */
  @NotNull
  public String getHttpStatus() {
    return httpStatus;
  }

  public void setHttpStatus(String httpStatus) {
    this.httpStatus = httpStatus;
  }

  public HttpErrorInfo message(String message) {
    this.message = message;
    return this;
  }

  /**
   * The reason for the error.
   * @return message
  */
  @NotNull
  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    HttpErrorInfo httpErrorInfo = (HttpErrorInfo) o;
    return Objects.equals(this.timestamp, httpErrorInfo.timestamp) &&
        Objects.equals(this.path, httpErrorInfo.path) &&
        Objects.equals(this.httpStatus, httpErrorInfo.httpStatus) &&
        Objects.equals(this.message, httpErrorInfo.message);
  }

  @Override
  public int hashCode() {
    return Objects.hash(timestamp, path, httpStatus, message);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class HttpErrorInfo {\n");
    
    sb.append("    timestamp: ").append(toIndentedString(timestamp)).append("\n");
    sb.append("    path: ").append(toIndentedString(path)).append("\n");
    sb.append("    httpStatus: ").append(toIndentedString(httpStatus)).append("\n");
    sb.append("    message: ").append(toIndentedString(message)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}
