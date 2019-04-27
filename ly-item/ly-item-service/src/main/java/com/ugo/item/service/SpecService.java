package com.ugo.item.service;

import com.ugo.common.utils.NumberUtils;
import com.ugo.item.mapper.SpecGroupMapper;
import com.ugo.item.mapper.SpecParamMapper;
import com.ugo.item.pojo.SpecGroup;
import com.ugo.item.pojo.SpecParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author: qc
 * @Date: 2019-4-23 16:20
 */
@Service
public class SpecService {
    @Autowired
    private SpecGroupMapper specGroupMapper;

    @Autowired
    private SpecParamMapper specParamMapper;

    public List<SpecGroup> querySpecGroupsByCid(Long cid) {
        SpecGroup specGroup = new SpecGroup();
        specGroup.setCid(cid);
        return specGroupMapper.select(specGroup);
    }

    public List<SpecParam> querySpecParamByGid(Long gid, Long cid, Boolean searching, Boolean generic) {
        SpecParam specParam = new SpecParam();
        specParam.setGroupId(gid);
        specParam.setCid(cid);
        specParam.setSearching(searching);
        specParam.setGeneric(generic);

        return specParamMapper.select(specParam);
    }

    public List<SpecGroup> querySpecsByCid(Long cid) {
        List<SpecGroup> specGroups = this.querySpecGroupsByCid(cid);

        specGroups.forEach(specGroup -> {
            specGroup.setParams(querySpecParamByGid(specGroup.getId(),null,null,null));
        });
        return specGroups;
    }

    public void saveSpecGroups(SpecGroup  specGroup) {
        //SpecGroup specGroup=new SpecGroup();
        specGroupMapper.insert(specGroup);
    }
}
