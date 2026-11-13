package com.v1.pension.repository.system;

import com.v1.pension.model.system.DocumentType;
import jakarta.persistence.Tuple;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DocumentTypeRepository extends JpaRepository<DocumentType, Long> {
  @Query(nativeQuery = true, value = """
      select id, document_type_name_nep as name
      from document_type
      where status = 1
      """)
  List<Tuple> getActiveDocumentTypes();
}
