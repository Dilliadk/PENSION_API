package com.v1.pension.repository.pensioner;

import com.v1.pension.model.pensioner.PensionerDocument;
import jakarta.persistence.Tuple;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PensionerDocumentRepository extends JpaRepository<PensionerDocument, Long> {
  @Query(nativeQuery = true, value = """
      select d.id, d.file_name, d.file_type, d.document_no, dt.document_type_name_nep
      from pensioner_documents d
      inner join document_type dt on dt.id = d.document_type_id
      where d.pension_subpno = :subPNo
      """)
  List<Tuple> getDocumentsForPensioner(@Param("subPNo") String subPNo);
}
