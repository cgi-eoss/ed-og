package com.cgi.eoss.osiris.model;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.google.common.collect.ComparisonChain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * <p>An association between OsirisFiles</p>
 */
@Data
@EqualsAndHashCode(exclude = {"id"})
@Table(name = "osiris_files_relations",
        indexes = {
                @Index(name = "osiris_files_relations_source_idx", columnList = "source_file"),
                @Index(name = "osiris_files_relations_target_idx", columnList = "target_file")},
	    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"source_file", "target_file", "type"})}
)
@NoArgsConstructor
@Entity
public class OsirisFilesRelation implements OsirisEntity<OsirisFilesRelation>{
	
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * <p>Reference to source file.</p>
     */
	@ManyToOne(fetch = FetchType.EAGER, cascade= {CascadeType.REMOVE})
    @JoinColumn(name = "source_file")
    private OsirisFile sourceFile;

    /**
     * <p>Reference to target file.</p>
     */
	@ManyToOne(fetch = FetchType.EAGER, cascade= {CascadeType.REMOVE})
    @JoinColumn(name = "target_file")
    private OsirisFile targetFile;

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private Type type;
    
    /**
     * <p>Construct a new OsirisFile instance with the minimum mandatory (and unique) parameters.</p>
     *
     * @param uri
     * @param restoId
     */
    public OsirisFilesRelation(OsirisFile source, OsirisFile target, Type type) {
        this.sourceFile = source;
        this.targetFile = target;
        this.type = type;
    }

    public OsirisFilesRelation(String reference) {
        // No-op, for SDR https://stackoverflow.com/questions/41324078/spring-data-rest-can-not-update-patch-a-list-of-child-entities-that-have-a-r
    }



    public enum Type {
        /**
         * <p>Source file is a visualization of target file</p>
         */

        VISUALIZATION_OF
    }



	@Override
	public int compareTo(OsirisFilesRelation fileRelation) {
		return ComparisonChain.start()
				.compare(sourceFile, fileRelation.sourceFile)
				.compare(targetFile, fileRelation.targetFile)
				.compare(type, fileRelation.sourceFile)
				.result();
	}
}
