/**
 * Autogenerated by Avro
 * 
 * DO NOT EDIT DIRECTLY
 */
package br.unb.cic.bionimbus.avro.gen;  
@SuppressWarnings("all")
@org.apache.avro.specific.AvroGenerated
public class Workflow extends org.apache.avro.specific.SpecificRecordBase implements org.apache.avro.specific.SpecificRecord {
  public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"Workflow\",\"namespace\":\"br.unb.cic.bionimbus.avro.gen\",\"fields\":[{\"name\":\"id\",\"type\":{\"type\":\"string\",\"avro.java.string\":\"String\"}},{\"name\":\"jobs\",\"type\":{\"type\":\"array\",\"items\":{\"type\":\"record\",\"name\":\"JobInfo\",\"fields\":[{\"name\":\"id\",\"type\":{\"type\":\"string\",\"avro.java.string\":\"String\"}},{\"name\":\"localId\",\"type\":{\"type\":\"string\",\"avro.java.string\":\"String\"}},{\"name\":\"serviceId\",\"type\":{\"type\":\"string\",\"avro.java.string\":\"String\"}},{\"name\":\"args\",\"type\":{\"type\":\"string\",\"avro.java.string\":\"String\"}},{\"name\":\"inputFiles\",\"type\":{\"type\":\"array\",\"items\":{\"type\":\"record\",\"name\":\"FileInfo\",\"fields\":[{\"name\":\"id\",\"type\":{\"type\":\"string\",\"avro.java.string\":\"String\"}},{\"name\":\"name\",\"type\":{\"type\":\"string\",\"avro.java.string\":\"String\"}},{\"name\":\"size\",\"type\":\"long\"},{\"name\":\"userId\",\"type\":\"long\"},{\"name\":\"uploadTimestamp\",\"type\":{\"type\":\"string\",\"avro.java.string\":\"String\"}},{\"name\":\"hash\",\"type\":{\"type\":\"string\",\"avro.java.string\":\"String\"}}]}}},{\"name\":\"outputs\",\"type\":{\"type\":\"array\",\"items\":{\"type\":\"string\",\"avro.java.string\":\"String\"}}},{\"name\":\"timestamp\",\"type\":\"long\"},{\"name\":\"dependencies\",\"type\":{\"type\":\"array\",\"items\":{\"type\":\"string\",\"avro.java.string\":\"String\"}}}]}}},{\"name\":\"creationDatestamp\",\"type\":{\"type\":\"string\",\"avro.java.string\":\"String\"}},{\"name\":\"userId\",\"type\":\"long\"},{\"name\":\"description\",\"type\":{\"type\":\"string\",\"avro.java.string\":\"String\"}},{\"name\":\"status\",\"type\":[{\"type\":\"enum\",\"name\":\"WorkflowStatus\",\"symbols\":[\"PENDING\",\"EXECUTING\",\"FINALIZED_WITH_SUCCESS\",\"FINALIZED_WITH_WARNINGS\",\"FINALIZED_WITH_ERRORS\",\"PAUSED\",\"STOPPED_WITH_ERROR\"]},\"null\"]}]}");
  public static org.apache.avro.Schema getClassSchema() { return SCHEMA$; }
  @Deprecated public java.lang.String id;
  @Deprecated public java.util.List<br.unb.cic.bionimbus.avro.gen.JobInfo> jobs;
  @Deprecated public java.lang.String creationDatestamp;
  @Deprecated public long userId;
  @Deprecated public java.lang.String description;
  @Deprecated public br.unb.cic.bionimbus.avro.gen.WorkflowStatus status;

  /**
   * Default constructor.
   */
  public Workflow() {}

  /**
   * All-args constructor.
   */
  public Workflow(java.lang.String id, java.util.List<br.unb.cic.bionimbus.avro.gen.JobInfo> jobs, java.lang.String creationDatestamp, java.lang.Long userId, java.lang.String description, br.unb.cic.bionimbus.avro.gen.WorkflowStatus status) {
    this.id = id;
    this.jobs = jobs;
    this.creationDatestamp = creationDatestamp;
    this.userId = userId;
    this.description = description;
    this.status = status;
  }

  public org.apache.avro.Schema getSchema() { return SCHEMA$; }
  // Used by DatumWriter.  Applications should not call. 
  public java.lang.Object get(int field$) {
    switch (field$) {
    case 0: return id;
    case 1: return jobs;
    case 2: return creationDatestamp;
    case 3: return userId;
    case 4: return description;
    case 5: return status;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }
  // Used by DatumReader.  Applications should not call. 
  @SuppressWarnings(value="unchecked")
  public void put(int field$, java.lang.Object value$) {
    switch (field$) {
    case 0: id = (java.lang.String)value$; break;
    case 1: jobs = (java.util.List<br.unb.cic.bionimbus.avro.gen.JobInfo>)value$; break;
    case 2: creationDatestamp = (java.lang.String)value$; break;
    case 3: userId = (java.lang.Long)value$; break;
    case 4: description = (java.lang.String)value$; break;
    case 5: status = (br.unb.cic.bionimbus.avro.gen.WorkflowStatus)value$; break;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }

  /**
   * Gets the value of the 'id' field.
   */
  public java.lang.String getId() {
    return id;
  }

  /**
   * Sets the value of the 'id' field.
   * @param value the value to set.
   */
  public void setId(java.lang.String value) {
    this.id = value;
  }

  /**
   * Gets the value of the 'jobs' field.
   */
  public java.util.List<br.unb.cic.bionimbus.avro.gen.JobInfo> getJobs() {
    return jobs;
  }

  /**
   * Sets the value of the 'jobs' field.
   * @param value the value to set.
   */
  public void setJobs(java.util.List<br.unb.cic.bionimbus.avro.gen.JobInfo> value) {
    this.jobs = value;
  }

  /**
   * Gets the value of the 'creationDatestamp' field.
   */
  public java.lang.String getCreationDatestamp() {
    return creationDatestamp;
  }

  /**
   * Sets the value of the 'creationDatestamp' field.
   * @param value the value to set.
   */
  public void setCreationDatestamp(java.lang.String value) {
    this.creationDatestamp = value;
  }

  /**
   * Gets the value of the 'userId' field.
   */
  public java.lang.Long getUserId() {
    return userId;
  }

  /**
   * Sets the value of the 'userId' field.
   * @param value the value to set.
   */
  public void setUserId(java.lang.Long value) {
    this.userId = value;
  }

  /**
   * Gets the value of the 'description' field.
   */
  public java.lang.String getDescription() {
    return description;
  }

  /**
   * Sets the value of the 'description' field.
   * @param value the value to set.
   */
  public void setDescription(java.lang.String value) {
    this.description = value;
  }

  /**
   * Gets the value of the 'status' field.
   */
  public br.unb.cic.bionimbus.avro.gen.WorkflowStatus getStatus() {
    return status;
  }

  /**
   * Sets the value of the 'status' field.
   * @param value the value to set.
   */
  public void setStatus(br.unb.cic.bionimbus.avro.gen.WorkflowStatus value) {
    this.status = value;
  }

  /** Creates a new Workflow RecordBuilder */
  public static br.unb.cic.bionimbus.avro.gen.Workflow.Builder newBuilder() {
    return new br.unb.cic.bionimbus.avro.gen.Workflow.Builder();
  }
  
  /** Creates a new Workflow RecordBuilder by copying an existing Builder */
  public static br.unb.cic.bionimbus.avro.gen.Workflow.Builder newBuilder(br.unb.cic.bionimbus.avro.gen.Workflow.Builder other) {
    return new br.unb.cic.bionimbus.avro.gen.Workflow.Builder(other);
  }
  
  /** Creates a new Workflow RecordBuilder by copying an existing Workflow instance */
  public static br.unb.cic.bionimbus.avro.gen.Workflow.Builder newBuilder(br.unb.cic.bionimbus.avro.gen.Workflow other) {
    return new br.unb.cic.bionimbus.avro.gen.Workflow.Builder(other);
  }
  
  /**
   * RecordBuilder for Workflow instances.
   */
  public static class Builder extends org.apache.avro.specific.SpecificRecordBuilderBase<Workflow>
    implements org.apache.avro.data.RecordBuilder<Workflow> {

    private java.lang.String id;
    private java.util.List<br.unb.cic.bionimbus.avro.gen.JobInfo> jobs;
    private java.lang.String creationDatestamp;
    private long userId;
    private java.lang.String description;
    private br.unb.cic.bionimbus.avro.gen.WorkflowStatus status;

    /** Creates a new Builder */
    private Builder() {
      super(br.unb.cic.bionimbus.avro.gen.Workflow.SCHEMA$);
    }
    
    /** Creates a Builder by copying an existing Builder */
    private Builder(br.unb.cic.bionimbus.avro.gen.Workflow.Builder other) {
      super(other);
    }
    
    /** Creates a Builder by copying an existing Workflow instance */
    private Builder(br.unb.cic.bionimbus.avro.gen.Workflow other) {
            super(br.unb.cic.bionimbus.avro.gen.Workflow.SCHEMA$);
      if (isValidValue(fields()[0], other.id)) {
        this.id = data().deepCopy(fields()[0].schema(), other.id);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.jobs)) {
        this.jobs = data().deepCopy(fields()[1].schema(), other.jobs);
        fieldSetFlags()[1] = true;
      }
      if (isValidValue(fields()[2], other.creationDatestamp)) {
        this.creationDatestamp = data().deepCopy(fields()[2].schema(), other.creationDatestamp);
        fieldSetFlags()[2] = true;
      }
      if (isValidValue(fields()[3], other.userId)) {
        this.userId = data().deepCopy(fields()[3].schema(), other.userId);
        fieldSetFlags()[3] = true;
      }
      if (isValidValue(fields()[4], other.description)) {
        this.description = data().deepCopy(fields()[4].schema(), other.description);
        fieldSetFlags()[4] = true;
      }
      if (isValidValue(fields()[5], other.status)) {
        this.status = data().deepCopy(fields()[5].schema(), other.status);
        fieldSetFlags()[5] = true;
      }
    }

    /** Gets the value of the 'id' field */
    public java.lang.String getId() {
      return id;
    }
    
    /** Sets the value of the 'id' field */
    public br.unb.cic.bionimbus.avro.gen.Workflow.Builder setId(java.lang.String value) {
      validate(fields()[0], value);
      this.id = value;
      fieldSetFlags()[0] = true;
      return this; 
    }
    
    /** Checks whether the 'id' field has been set */
    public boolean hasId() {
      return fieldSetFlags()[0];
    }
    
    /** Clears the value of the 'id' field */
    public br.unb.cic.bionimbus.avro.gen.Workflow.Builder clearId() {
      id = null;
      fieldSetFlags()[0] = false;
      return this;
    }

    /** Gets the value of the 'jobs' field */
    public java.util.List<br.unb.cic.bionimbus.avro.gen.JobInfo> getJobs() {
      return jobs;
    }
    
    /** Sets the value of the 'jobs' field */
    public br.unb.cic.bionimbus.avro.gen.Workflow.Builder setJobs(java.util.List<br.unb.cic.bionimbus.avro.gen.JobInfo> value) {
      validate(fields()[1], value);
      this.jobs = value;
      fieldSetFlags()[1] = true;
      return this; 
    }
    
    /** Checks whether the 'jobs' field has been set */
    public boolean hasJobs() {
      return fieldSetFlags()[1];
    }
    
    /** Clears the value of the 'jobs' field */
    public br.unb.cic.bionimbus.avro.gen.Workflow.Builder clearJobs() {
      jobs = null;
      fieldSetFlags()[1] = false;
      return this;
    }

    /** Gets the value of the 'creationDatestamp' field */
    public java.lang.String getCreationDatestamp() {
      return creationDatestamp;
    }
    
    /** Sets the value of the 'creationDatestamp' field */
    public br.unb.cic.bionimbus.avro.gen.Workflow.Builder setCreationDatestamp(java.lang.String value) {
      validate(fields()[2], value);
      this.creationDatestamp = value;
      fieldSetFlags()[2] = true;
      return this; 
    }
    
    /** Checks whether the 'creationDatestamp' field has been set */
    public boolean hasCreationDatestamp() {
      return fieldSetFlags()[2];
    }
    
    /** Clears the value of the 'creationDatestamp' field */
    public br.unb.cic.bionimbus.avro.gen.Workflow.Builder clearCreationDatestamp() {
      creationDatestamp = null;
      fieldSetFlags()[2] = false;
      return this;
    }

    /** Gets the value of the 'userId' field */
    public java.lang.Long getUserId() {
      return userId;
    }
    
    /** Sets the value of the 'userId' field */
    public br.unb.cic.bionimbus.avro.gen.Workflow.Builder setUserId(long value) {
      validate(fields()[3], value);
      this.userId = value;
      fieldSetFlags()[3] = true;
      return this; 
    }
    
    /** Checks whether the 'userId' field has been set */
    public boolean hasUserId() {
      return fieldSetFlags()[3];
    }
    
    /** Clears the value of the 'userId' field */
    public br.unb.cic.bionimbus.avro.gen.Workflow.Builder clearUserId() {
      fieldSetFlags()[3] = false;
      return this;
    }

    /** Gets the value of the 'description' field */
    public java.lang.String getDescription() {
      return description;
    }
    
    /** Sets the value of the 'description' field */
    public br.unb.cic.bionimbus.avro.gen.Workflow.Builder setDescription(java.lang.String value) {
      validate(fields()[4], value);
      this.description = value;
      fieldSetFlags()[4] = true;
      return this; 
    }
    
    /** Checks whether the 'description' field has been set */
    public boolean hasDescription() {
      return fieldSetFlags()[4];
    }
    
    /** Clears the value of the 'description' field */
    public br.unb.cic.bionimbus.avro.gen.Workflow.Builder clearDescription() {
      description = null;
      fieldSetFlags()[4] = false;
      return this;
    }

    /** Gets the value of the 'status' field */
    public br.unb.cic.bionimbus.avro.gen.WorkflowStatus getStatus() {
      return status;
    }
    
    /** Sets the value of the 'status' field */
    public br.unb.cic.bionimbus.avro.gen.Workflow.Builder setStatus(br.unb.cic.bionimbus.avro.gen.WorkflowStatus value) {
      validate(fields()[5], value);
      this.status = value;
      fieldSetFlags()[5] = true;
      return this; 
    }
    
    /** Checks whether the 'status' field has been set */
    public boolean hasStatus() {
      return fieldSetFlags()[5];
    }
    
    /** Clears the value of the 'status' field */
    public br.unb.cic.bionimbus.avro.gen.Workflow.Builder clearStatus() {
      status = null;
      fieldSetFlags()[5] = false;
      return this;
    }

    @Override
    public Workflow build() {
      try {
        Workflow record = new Workflow();
        record.id = fieldSetFlags()[0] ? this.id : (java.lang.String) defaultValue(fields()[0]);
        record.jobs = fieldSetFlags()[1] ? this.jobs : (java.util.List<br.unb.cic.bionimbus.avro.gen.JobInfo>) defaultValue(fields()[1]);
        record.creationDatestamp = fieldSetFlags()[2] ? this.creationDatestamp : (java.lang.String) defaultValue(fields()[2]);
        record.userId = fieldSetFlags()[3] ? this.userId : (java.lang.Long) defaultValue(fields()[3]);
        record.description = fieldSetFlags()[4] ? this.description : (java.lang.String) defaultValue(fields()[4]);
        record.status = fieldSetFlags()[5] ? this.status : (br.unb.cic.bionimbus.avro.gen.WorkflowStatus) defaultValue(fields()[5]);
        return record;
      } catch (Exception e) {
        throw new org.apache.avro.AvroRuntimeException(e);
      }
    }
  }
}
