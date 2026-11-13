package com.v1.pension.repository.pensioner;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.v1.pension.model.pensioner.DigitallySignedDocument;

public interface DigitallySignedDocumentRepository extends JpaRepository<DigitallySignedDocument, Long> {
  @Query(nativeQuery = true, value = """
      select * from digitally_signed_documents
      where table_name = :tableName and type = :type and record_id = :recordId
      order by id desc limit 1
      """)
  Optional<DigitallySignedDocument> findByTypeAndTableNameAndRecordId(String type, String tableName, long recordId);

  @Query(nativeQuery = true, value = """
      select exists (
        select 1 from digitally_signed_documents
        where table_name = :tableName and type = :type and record_id = :recordId
      )
      """)
  boolean existsByTypeTableNameAndRecordId(String type, String tableName, long recordId);
}
