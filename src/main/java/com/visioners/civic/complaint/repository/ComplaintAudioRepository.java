package com.visioners.civic.complaint.repository;

import com.visioners.civic.complaint.entity.ComplaintAudio;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

@Repository
public interface ComplaintAudioRepository extends JpaRepository<ComplaintAudio, Long> {

    Optional<ComplaintAudio> findByComplaintId(Long complaintId);
}
