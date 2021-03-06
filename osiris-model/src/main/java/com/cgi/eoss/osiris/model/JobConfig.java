package com.cgi.eoss.osiris.model;

import com.cgi.eoss.osiris.model.converters.StringMultimapYamlConverter;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * <p>Configuration representing the inputs of a specific service execution.</p>
 * <p>This object is suitable for sharing for re-execution, independent of the actual run status and outputs.</p>
 */
@Data
@EqualsAndHashCode(exclude = {"id","parent"})
@ToString(exclude = {"inputs", "parent"})
@Table(name = "osiris_job_configs",
        indexes = {@Index(name = "osiris_job_configs_service_idx", columnList = "service"), @Index(name = "osiris_job_configs_owner_idx", columnList = "owner"), @Index(name = "osiris_job_configs_label_idx", columnList = "label")},
        uniqueConstraints = @UniqueConstraint(columnNames = {"owner", "service", "inputs", "parent", "systematic_parameter"}))
@NoArgsConstructor
@Entity
public class JobConfig implements OsirisEntityWithOwner<JobConfig> {

    /**
     * <p>Unique identifier of the job.</p>
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * <p>The user owning the job configuration.</p>
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "owner", nullable = false)
    private User owner;
    
    /**
     * <p>Parent job to attach to, if any</p>
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "parent")
    private Job parent;

    /**
     * <p>The service this job is configuring.</p>
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "service", nullable = false)
    private OsirisService service;

    /**
     * <p>The job input parameters.</p>
     */
    @Lob
    @Convert(converter = StringMultimapYamlConverter.class)
    @Column(name = "inputs")
    private Multimap<String, String> inputs = HashMultimap.create();

    /**
     * <p>Human-readable label to tag and identify jobs launched with this configuration.</p>
     */
    @Column(name = "label")
    private String label;
    
    /**
     * <p>Tag and identify parameter that will be dynamically getting values </p>
     */
    @Column(name = "systematic_parameter")
    private String systematicParameter;

    /**
     * <p>Create a new JobConfig instance with the minimum required parameters.</p>
     *
     * @param owner The user who owns the job
     * @param service The service this job is running on
     */
    public JobConfig(User owner, OsirisService service) {
        this.owner = owner;
        this.service = service;
    }

    @Override
    public int compareTo(JobConfig o) {
        return ComparisonChain.start().compare(service, o.service).result();
    }

}
