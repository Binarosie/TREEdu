package vn.hcmute.edu.materialsservice.dtos.response;



import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DataTableResponse<T> {
    private int draw;
    private long recordsTotal;
    private long recordsFiltered;
    private List<T> data;
}