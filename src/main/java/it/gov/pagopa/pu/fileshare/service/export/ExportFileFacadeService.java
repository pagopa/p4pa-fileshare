package it.gov.pagopa.pu.fileshare.service.export;

import it.gov.pagopa.pu.fileshare.dto.FileResourceDTO;
import it.gov.pagopa.pu.p4paauth.dto.generated.UserInfo;
import java.io.FileNotFoundException;

public interface ExportFileFacadeService {
  FileResourceDTO downloadExportFile(Long organizationId, Long exportFileId, UserInfo user, String accessToken)
    throws FileNotFoundException;

}
