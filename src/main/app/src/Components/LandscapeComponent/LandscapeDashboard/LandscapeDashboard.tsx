import React, { useState, useEffect, ReactElement, MouseEvent } from 'react';
import { useParams } from 'react-router-dom';

import LandscapeDashboardLayout from './LandscapeDashboardLayout';
import Slider from '../../SliderComponent/Slider';
import { ILandscape, IItem, IAssessment } from '../../../interfaces';
import { get } from '../../../utils/API/APIClient';
import LandscapeItem from '../LandscapeItem/LandscapeItem';
import { CSSTransition } from 'react-transition-group';

/**
 * Logic Component to display all available landscapes
 */

const LandscapeDashboard: React.FC = () => {
  const [landscape, setLandscape] = useState<ILandscape | null>();
  const [sliderContent, setSliderContent] = useState<string | ReactElement | null>(null);
  const [showSlider, setShowSlider] = useState(false);
  const [assessments, setAssessments] = useState<IAssessment | null>(null);

  const onItemClick = (e: MouseEvent<HTMLSpanElement>, item: IItem) => {
    setSliderContent(
      <LandscapeItem fullyQualifiedItemIdentifier={item.fullyQualifiedIdentifier} />
    );
    setShowSlider(true);
  };

  const closeSlider = () => {
    setShowSlider(false);
  };

  const { landscapeIdentifier } = useParams();

  useEffect(() => {
    get(`/api/${landscapeIdentifier}`).then((response) => {
      setLandscape(response);
    });

    get(`/assessment/${landscapeIdentifier}`).then((response) => {
      setAssessments(response);
    });
  }, [landscapeIdentifier]);

  return (
    <div className='landscapeContainer'>
      <CSSTransition
        in={showSlider}
        timeout={{ enter: 0, exit: 1000, appear: 1000 }}
        appear
        unmountOnExit
        classNames='slider'
      >
        <Slider sliderContent={sliderContent} closeSlider={closeSlider} />
      </CSSTransition>
      <LandscapeDashboardLayout
        landscape={landscape}
        assessments={assessments}
        onItemClick={onItemClick}
      />
    </div>
  );
};

export default LandscapeDashboard;