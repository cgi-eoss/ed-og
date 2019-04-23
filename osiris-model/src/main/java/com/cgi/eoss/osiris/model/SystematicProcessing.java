package com.cgi.eoss.osiris.model;

import com.cgi.eoss.osiris.model.converters.StringListMultimapYamlConverter;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.ListMultimap;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(exclude = {"id"})
@Table(name = "osiris_systematic_processings",
        indexes = {@Index(name = "osiris_systematic_processing_owner_idx", columnList = "owner")})
@NoArgsConstructor
@Entity
public class SystematicProcessing implements OsirisEntityWithOwner<SystematicProcessing>{

    /**
     * <p>Internal unique identifier of the systematic processing.</p>
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * <p>The user owning the service, typically the systematic processing creator.</p>
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "owner", nullable = false)
    private User owner;
    
    /**
     * <p>The current execution status of the job.</p>
     */
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private Status status = Status.ACTIVE;
    
    
    /**
     * <p>The job that will contain all systematic subjobs</p>
     */
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "parentJob", nullable = false)
    private Job parentJob;
    
    
    @Lob
    @Convert(converter = StringListMultimapYamlConverter.class)
    @Column(name = "search_parameters")
    private ListMultimap<String, String> searchParameters;
    
    @Column(name = "cron_expression")
    private String cronExpression;
    
    /**
    * <p>The date and time this processing was last updated.</p>
    */
   @Column(name = "last_updated")
   private LocalDateTime lastUpdated;
    
    @Override
    public int compareTo(SystematicProcessing o) {
        return ComparisonChain.start().compare(owner.getId(), o.owner.getId()).result();
    }
    
    public enum Status {
        ACTIVE, BLOCKED, COMPLETED
    }

    public SystematicProcessing(User owner, Job parentJob, ListMultimap<String, String> searchParameters, String cronExpression, LocalDateTime lastUpdated) {
        this.owner = owner;
        this.parentJob = parentJob;
        this.searchParameters = searchParameters;
        this.cronExpression = cronExpression;
        this.lastUpdated = lastUpdated;
    }
}
